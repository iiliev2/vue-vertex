<template>
<div class="col-lg-5" style="padding-top:8px;">
  <el-table ref="singleTable" :data="tableData" @selection-change="handleSelectionChange" highlight-current-row style="width: 100%">
    <el-table-column type="selection" width="50" />
    <el-table-column v-for="column in tableColumns" :prop="column" :label='column | removeSpecialChars | capitalize' />
    <el-table-column label="Operations">
      <template scope="scope">
        <el-button
          size="small"
          @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
        <el-button
          size="small"
          type="danger"
          @click="handleDelete(scope.$index, scope.row)">Delete</el-button>
      </template>
    </el-table-column>
  </el-table>

  <div class="block">
    <el-pagination layout="slot, prev, pager, next" :total="1">
      <delete v-if="checked.length>0" message="Are you sure you want to delete these users?" @delete-accepted="deleteAccepted" @delete-canceleted="deleteCanceled" />
    </el-pagination>
  </div>

</div>
</template>

<script>
import Delete from './Delete.vue'

export default {
  data() {
    return {
      tableColumns: ['_id', 'firstName', 'surname', 'lastName', 'version'],
      currentRow: null,
      checked: []
    }
  },
  components: {
    Delete
  },
  props: {
    'tableData': {
      type: Array,
      required: true
    }
  },
  methods: {
    'deleteAccepted': function() {
      this.$emit('delete-accepted');
    },
    'deleteCanceled': function() {},
    handleEdit(index, row) {
      console.log(index, row);
    },
    handleDelete(index, row) {
      console.log(index, row);
    },
    handleSelectionChange(val) {
      this.checked = val;
    }
  },
  filters: {
    capitalize: function(value) {
      if (!value) return ''
      value = value.toString()
      return value.charAt(0).toUpperCase() + value.slice(1)
    },
    removeSpecialChars: function(value) {
      if (!value) return ''
      value = value.toString()
      return value.replace(/[^a-zA-Z ]/g, "")
    }
  }
}
</script>
