<template>
<span>
  <el-button @click="showMe = true" :class="invokeButtonIcon"/>
  <el-dialog
          title="Tip"
          :visible.sync="showMe"
          size="tiny">
  <span>{{ message }}</span>
  <span slot="footer" class="wrapper">
    <el-button @click="clickedOK()">OK</el-button>
    <el-button @click="clickedCancel()">Cancel</el-button>
  </span>
</el-dialog>

</span>
</template>

<script>
    export default {
        data() {
            return {
                showMe: false
            }
        },
        props: {
            message: {
                type: String,
                required: true
            },
            invokeButtonIcon: {
                type: String,
                required: true
            }
        },
        methods: {
            'clickedOK': function () {
                this.$emit('accepted')
                this.showMe = false;
            },
            'clickedCancel': function () {
                this.$emit('rejected')
                this.showMe = false;
            }
        }
    }
</script>
