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
                            v-on:click.prevent="handleCreateOrEdit(scope.$index, scope.row)"
                            class="el-icon-edit component-display-nonvisible"/>
                </template>
            </el-table-column>
        </el-table>
        <!--Pagination-->
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
                currentCell: Object,
                changedCells: [],
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
            'deleteAccepted': function () {
                this.$emit('delete-accepted');
            },
            handleCellFocus(event) {
                if (event.target !== this.currentCell) {
                    this.currentCell = event.target;
                }
            },
            handleCellChange(event, index) {
                this.manageCurrentRowEditButtonStyleClasses(index, "component-display-nonvisible", "component-display-visible");

                let currentCellClassName = this.currentCell.className;
                if (currentCellClassName.indexOf("changed-input-text") === -1) {
                    this.currentCell.className += " changed-input-text";
                }
                if (this.changedCells.indexOf(this.currentCell) === -1) {
                    this.changedCells.push(this.currentCell);
                }
            },
            handleCreateOrEdit(index, row) {
                if (row[this.tableColumns[0].colname]){
                    this.emitEdit(index, row);
                } else {
                    this.emitCreate(index, row);
                }
            },
            emitEdit(index, row) {
                this.resetChangedCellsStyleClass();

                this.manageCurrentRowEditButtonStyleClasses(index, "component-display-visible", "component-display-nonvisible");

                this.$emit('edit-executed', index, row);
            },
            emitCreate(index, row) {
                this.resetChangedCellsStyleClass();

                this.manageCurrentRowEditButtonStyleClasses(index, "component-display-visible", "component-display-nonvisible");

                this.$emit('create-executed', index, row);
            },
            handleDelete(index, row) {
                this.$emit('delete-executed', index, row);
            },
            handleSelectionChange(val) {
                this.checked = val;
            },
            createNewTableRow() {
                let columnNames = this.tableColumns.map(function(tableColumnElement) {
                    return tableColumnElement.colname;
                });
                let ui = JSON.stringify(columnNames).replace(/\[|\]/g, "").replace(/,/g, ':"",');
                ui = '{' + ui + ':""}';
                this.tableData.push(JSON.parse(ui));
            },
            manageCurrentRowEditButtonStyleClasses(index, oldStyleClass, newStyleClass) {
                let elementById = document.getElementById('savebutton-' + index);
                elementById.className = elementById.className.replace(oldStyleClass, newStyleClass);
            },
            resetChangedCellsStyleClass() {
                this.changedCells.forEach(function (tableCell) {
                    tableCell.className = tableCell.className.replace("changed-input-text", "");
                });
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