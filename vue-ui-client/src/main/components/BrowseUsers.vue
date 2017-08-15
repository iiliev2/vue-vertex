<template>
    <div>
        <search :executeSearch='searchUsers'></search>
        <display-table :tableData='users' :tableColumns="visibleColumns" @delete-accepted="deleteAccepted"
                       v-if="error===''" @edit-executed='editUser'
                       @delete-executed='deleteUser' @create-executed="createUser">
        </display-table>
        <div v-else>{{error}}</div>
    </div>
</template>

<script>
    import config from '../js/index.js'
    import Search from './Search.vue'
    import DisplayTable from './DisplayTable.vue'

    export default {
        data() {
            return {
                users: [],
                error: '',
                serviceState: '',
                messagePattern: 'User ',
                serviceUrl: 'http://10.82.200.203:23002/api/users',
                visibleColumns: [
                    {colname: '_id', editable: false},
                    {colname: 'version', editable: false},
                    {colname: 'firstName', editable: true},
                    {colname: 'surname', editable: true},
                    {colname: 'lastName', editable: true}
                ]
            }
        },
        components: {
            Search,
            DisplayTable
        },
        methods: {
            deleteAccepted() {
                alert("Delete selected is not implemented, yet!");
            },
            setServiceState(serviceState) {
                this.serviceState = serviceState;
            },
            setServiceMessagePattern(messagePattern) {
                this.messagePattern = messagePattern;
            },
            resetServiceMessage() {
                this.setServiceState('');
                this.setServiceMessagePattern('User ');
            },
            getAllUsers() {
                this.setServiceState('loaded');
                this.setServiceMessagePattern('Users ');

                this.submit('get', this.serviceUrl).then(response => {
                    this.users = response;
                    this.resetServiceMessage();
                });
            },
            searchUsers(query) {
                this.setServiceState('filtered');
                this.setServiceMessagePattern('Users ');

                let searchUrl = this.serviceUrl;
                if (query) {
                    searchUrl += '?search_by_all_names_partial=' + query;
                }

                this.submit('get', searchUrl).then(response => {
                    this.users = response;
                    this.resetServiceMessage();
                });
            },
            editUser(index, user) {
                this.setServiceState('edited');

                this.submit('put',
                    this.serviceUrl + "/" + user['_id'], user
                ).then(response => {
                    this.users.splice(index, 1, response);
                    this.resetServiceMessage();
                });
            },
            deleteUser(index, user) {
                this.setServiceState('deleted');

                this.submit('delete',
                    this.serviceUrl + "/" + user['_id']
                ).then(response => {
                    this.users.splice(index, 1);
                    this.resetServiceMessage();
                });

            },
            createUser(index, user) {
                this.setServiceState('created');
                this.submit('post',
                            this.serviceUrl,
                            user
                ).then(response => {
                    this.users.splice(index, 1, response);
                    this.resetServiceMessage();
                });
            },
            submit(requestType, url, submitData, ) {
                return new Promise((resolve, reject) => {
                    this.$http[requestType](url, submitData)
                        .then(response => {
                            this.error = '';

                            if (this.successServiceMessage) {
                                console.log('successServiceMessage:' + this.successServiceMessage);
                                console.log('serviceState:' + this.serviceState);
                                this.$message({
                                    message: this.successServiceMessage,
                                    type: 'success'
                                });
                            }

                            resolve(response.data);
                        })
                        .catch(error => {
                            this.users = [];
                            this.error = "Could not fetch the users. " + error.message;

                            if (this.failServiceMessage) {
                                this.$message.error(this.failServiceMessage);
                            }

                            reject(error.response.data);
                        })
                });
            }
        },
        computed: {
            successServiceMessage: function () {
                return this.messagePattern + this.serviceState;
            },
            failServiceMessage: function () {
                return this.messagePattern + 'not ' + this.serviceState;
            }
        },
        mounted: function () {
            this.getAllUsers()
        }
    }
</script>
<style scoped>
    #allusers {
        background: limegreen;
        margin: 0px 0px 20px 0px;
    }
</style>
