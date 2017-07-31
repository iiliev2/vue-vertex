<template>
    <div>
        <search :executeSearch='searchUsers'></search>
        <display-table :tableData='items' @delete-accepted="deleteAccepted" v-if="error===''" @edit-executed='editUser'
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
                items: [],
                error: '',
                state: '',
                searchServiceUrl: 'http://10.82.200.203:23002/api/users'
            }
        },
        components: {
            Search,
            DisplayTable
        },
        methods: {
            'deleteAccepted': function () {
                alert("Delete selected is not implemented, yet!");
                //this.getAllUsers()
            },
            'getAllUsers': function () {
                this.$http.get(this.searchServiceUrl)
                    .then(response => {
                        this.items = response.data;
                        this.error = ''
                    })
                    .catch(error => {
                        this.items = [];
                        this.error = "Could not fetch the users. " + error.message
                    })
            },
            'viewUser': function (user) {
                config.router.push('/user/:' + user.id)
            },
            'searchUsers': function (query) {
                let searchUrl = this.searchServiceUrl;

                if (query) {
                    searchUrl += '?search_by_all_names_partial=' + query;
                }

                this.$http.get(searchUrl)
                    .then(response => {
                        this.items = response.data;
                        this.error = ''
                    })
                    .catch(error => {
                        this.items = [];
                        this.error = "Could not fetch the users. " + error.message
                    })
            },
            editUser(index, user) {
                this.$http.put(this.searchServiceUrl + "/" + user['_id'], user)
                    .then(response => {
                        this.items.splice(index, 1, response.data.$set);
                        this.$message({
                                          message: 'User edited',
                                          type: 'success'
                                      });
                    })
                    .catch(error => {
                        this.items = [];
                        this.error = "Could not fetch the users. " + error.message;
                        this.$message.error('User not edited');
                    });
            },
            deleteUser(index, user) {
                this.$http.delete(this.searchServiceUrl + "/" + user['_id'])
                    .then(response => {
                        this.getAllUsers();
                        this.$message({
                                          message: 'User deleted',
                                          type: 'success'
                                      });
                    })
                    .catch(error => {
                        this.items = [];
                        this.error = "Could not fetch the users. " + error.message;
                        this.$message.error('User not deleted');
                    });
            },
            createUser(index, user) {
                this.$http.post(this.searchServiceUrl, user)
                    .then(response => {
                        this.getAllUsers();
                        this.$message({
                                          message: 'User created',
                                          type: 'success'
                                      });
                    })
                    .catch(error => {
                        this.items = [];
                        this.error = "Could not fetch the users. " + error.message;
                        this.$message.error('User not created');
                    });
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
