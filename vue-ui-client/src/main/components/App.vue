<template>
<div id="app2">
	<router-link v-bind:to="'/all_users'">All Users</router-link>
	<router-link v-bind:to="'/user/1'">User</router-link>
	<router-view></router-view>
</div>
</template>

<script>
export default {

  data() {
    return {
      
    }
  }
}
</script>
<style></style>