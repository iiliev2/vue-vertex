<template>
<div>
  <search :executeSearch='searchUsers'></search>
  <table id="allusers" v-if="error===''">
    <tr v-for="item in items">
      <td>
        <input type="checkbox" :value="item.id" v-model="checked">
      </td>
      <div @click="viewUser(item)">
        <td v-for="value in item">{{value}}</td>
      </div>
    </tr>
  </table>
  <div v-else>{{error}}</div>

  <router-link :to="'/create_user'" tag="button">Create User</router-link>
  <delete v-if="checked.length>0" message="Are you sure you want to delete these users?" @delete-accepted="deleteAccepted" @delete-canceleted="deleteCanceled">
  </delete>
</div>
</template>

<script>
import config from '../js/index.js'
import Delete from './Delete.vue'
import Search from './Search.vue'

export default {
  data() {
    return {
      items: [],
      error: '',
      checked: [],
      searchServiceUrl: 'http://localhost:23002/api/users'
    }
  },
  components: {
    'delete': Delete,
    Search
  },
  methods: {
    'deleteAccepted': function() {
      this.checked = []
      this.getAllUsers()
    },
    'deleteCanceled': function() {
      this.checked = []
      this.getAllUsers()
    },
    'getAllUsers': function() {
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
    'viewUser': function(user) {
      config.router.push('/user/:' + user.id)
    },
    'searchUsers': function(query) {
      var searchUrl = this.searchServiceUrl;

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
    }
  },
  mounted: function() {
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
