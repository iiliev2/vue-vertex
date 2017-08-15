<template>
<div class="col-lg-5" style="padding-top:8px;">
  <!-- The search field -->
  <form class="form-horizontal">
    <div class="input-group">
      <el-input
              placeholder="Please type here"
              icon="search"
              v-model.trim="query"
              :on-icon-click="handleIconClick"
              @keyup.enter.native="handleEnter"
              @input="handleExecuteSearch"
              autofocus style="width: 200px"
              auto-complete="on"/>
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
  },
  methods: {
      handleEnter(ev) {
          this.executeSearch(this.query);
      },
      handleIconClick(ev) {
          this.executeSearch(this.query);
      },
      handleExecuteSearch(ev) {
          let queryLength = this.query.length;
          if (queryLength > 0 && queryLength < 3) return;

          this.executeSearch(this.query);
      }
  }
}
</script>
<style>
[v-cloak] {
  display: none;
}
</style>
