<template>
    <div class="col-lg-5" style="padding-top:8px;">
        <el-table ref="singleTable" :data="tableData" @selection-change="handleSelectionChange" highlight-current-row
                  style="width: 100%">
            <el-table-column type="selection" width="50"/>
            <el-table-column v-for="columnItem in tableColumns"
                             :label="columnItem.colname | removeSpecialChars | capitalize">
                <template scope="props">
                    <el-input v-model='props.row[columnItem.colname]' :readonly='!columnItem.editable'
                              @focus="handleCellFocus" @change="handleCellChange(event, props.$index)"/>
                </template>
            </el-table-column>
            <el-table-column label="Operations">
                <template scope="scope">
                    <el-button
                            size="small"
                            type="danger"
                            @click="handleDelete(scope.$index, scope.row)" class="el-icon-delete"/>
                    <el-button
                            :id="'savebutton-' + scope.$index"
                            size="small"
                            v-on:click.prevent="executeSave(scope.row)" class="el-icon-edit component-display-nonvisible"/>
                </template>
            </el-table-column>
        </el-table>

        <span class="block">
    <el-pagination layout="prev, pager, next, slot" :total="1">
      <span>
      <el-button size="small" @click="createNewTableRow()" class="el-icon-edit"/>
      <delete v-if="checked.length>0" message="Are you sure you want to delete these users?"
              @delete-accepted="deleteAccepted"/>
      </span>
    </el-pagination>
  </span>
    </div>
</template>

<script>
    import Delete from './Delete.vue'

    export default {
        data() {
            return {
                tableColumns: [
                    {colname: '_id', editable: false},
                    {colname: 'version', editable: false},
                    {colname: 'firstName', editable: true},
                    {colname: 'surname', editable: true},
                    {colname: 'lastName', editable: true}
                ],
                checked: [],
                currentCell: Object
            }
        },
        components: {
            Delete
        },
        props: {
            'tableData': {
                type: Array,
                required: true
            },
            'executeSave': {
                type: Function,
                required: true
            }
        },
        methods: {
            'deleteAccepted': function () {
                this.$emit('delete-accepted');
            },
            handleCellFocus(event) {
                if(event.target !== this.currentCell){
                    this.currentCell = event.target;
                }
            },
            handleCellChange(event, index) {
                let elementById = document.getElementById('savebutton-' + index);
                elementById.className = elementById.className.replace("component-display-nonvisible", "component-display-visible");
                this.currentCell.className += " changed-input-text";
            },
            handleDelete(index, row) {
                alert("Delete current not implemented, yet!");
            },
            handleSelectionChange(val) {
                this.checked = val;
            },
            createNewTableRow() {
                this.tableData.push("");
            }
        },
        filters: {
            capitalize: function (value) {
                if (!value) return ''
                value = value.toString()
                return value.charAt(0).toUpperCase() + value.slice(1)
            },
            removeSpecialChars: function (value) {
                if (!value) return ''
                value = value.toString()
                return value.replace(/[^a-zA-Z ]/g, "")
            }
        }
    }
</script>
<style>
    .component-display-nonvisible {
        display: none
    }
    .component-display-visible {
        display: inline;
    }
    .changed-input-text {
        border: 2px solid green;
        border-radius: 4px;
    }
</style>