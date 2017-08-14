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
            getAllUsers() {
                this.submit('get', this.serviceUrl).then(response => {
                    this.users = response;
                });
            },
            searchUsers(query) {
                let searchUrl = this.serviceUrl;

                if (query) {
                    searchUrl += '?search_by_all_names_partial=' + query;
                }

                this.submit('get', searchUrl).then(response => {
                    this.users = response;
                });
            },
            editUser(index, user) {
                this.serviceState = 'edited';
                this.submit('put',
                    this.serviceUrl + "/" + user['_id'], user
                ).then(response => {
                    this.users.splice(index, 1, response);
                });
            },
            deleteUser(index, user) {
                this.serviceState = 'deleted';
                this.submit('delete',
                    this.serviceUrl + "/" + user['_id']
                ).then(response => {
                    this.users.splice(index, 1);
                });

            },
            createUser(index, user) {
                this.serviceState = 'created';
                this.submit('post',
                            this.serviceUrl,
                            user
                ).then(response => {
                    this.users.splice(index, 1, response);
                });
            },
            submit(requestType, url, submitData) {
                return new Promise((resolve, reject) => {
                    this.$http[requestType](url, submitData)
                        .then(response => {
                            this.error = '';

                            if (this.successServiceMessage) {
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
                return 'User ' + this.serviceState;
            },
            failServiceMessage: function () {
                return 'User not ' + this.serviceState;
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
