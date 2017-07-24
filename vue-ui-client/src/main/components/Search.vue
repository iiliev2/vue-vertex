<template>
<div class="col-lg-5" style="padding-top:8px;">
  <!-- The search field -->
  <form class="form-horizontal">
    <div class="input-group">
      <input id="queryField" v-model.trim="query" v-on:keypress.enter.prevent="executeSearch(query)" type="search" placeholder="please enter your query here." autofocus />
      <div title="Search for users">
        <button id="searchButton" type="button" v-on:click.prevent="executeSearch(query)">Search</button>
      </div>
    </div>
  </form>
</div>
</template>

<script>
export default {
  data() {
    return {
      query: ''
    }
  },
  props: {
    'executeSearch': {
      type: Function,
      required: true
    }
  }
}
</script>
<style>
[v-cloak] {
  display: none;
}
</style>
