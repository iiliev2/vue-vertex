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
              :keypress="handleExecuteSearch(query)"
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
      handleIconClick(ev) {
          this.executeSearch(this.query);
      },
      handleExecuteSearch(query) {
          let queryLength = query.length;
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
