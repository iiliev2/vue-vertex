<template>
<span>
  <el-button @click="showDelete = true" class="el-icon-delete"/>

  <el-dialog
  title="Tips"
  :visible.sync="showDelete"
  size="tiny"
  :before-close="handleClose">
  <span>{{ message }}</span>
<span slot="footer" class="dialog-footer">
  <el-button type="primary" @click="clickedOK()">OK</el-button>
  <el-button @click="clickedCancel()">Cancel</el-button>
  </span>
</el-dialog>

</span>
</template>

<script>
export default {
  data() {
    return {
      showDelete: false
    }
  },
  props: {
    'message': {
      type: String,
      required: true
    },
    'buttonLabel': {
      type: String,
      required: false,
      default: 'Delete'
    }
  },
  methods: {
    'clickedOK': function() {
      this.$emit('delete-accepted')
      this.showDelete = false;
    },
    'clickedCancel': function() {
      this.$emit('delete-canceled')
      this.showDelete = false;
    }
  }
}
</script>
