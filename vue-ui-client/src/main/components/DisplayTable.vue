<template>
    <div class="col-lg-5" style="padding-top:8px;">
        <el-table ref="singleTable" :data="tableData" @selection-change="handleSelectionChange"
                  highlight-current-row
                  style="width: 100%">
            <el-table-column type="selection" width="50"/>
            <el-table-column v-for="columnItem in tableColumns"
                             :label="columnItem.colname | removeSpecialChars | capitalize">
                <template scope="props">
                    <el-input :name="'input-' + columnItem.colname + '-' + props.$index"
                            v-model='props.row[columnItem.colname]' :readonly='!columnItem.editable'
                              @focus="handleCellFocus" @change="handleCellChange(props.$index)"/>
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
                            @click.prevent="handleCreateOrEdit(scope.$index, scope.row)"
                            class="el-icon-edit component-display-nonvisible"/>
                </template>
            </el-table-column>
        </el-table>
        <!--Pagination-->
        <span class="block">
            <el-pagination layout="prev, pager, next, slot" :total="1">
              <span>
              <el-button size="small" @click="addNew()" class="el-icon-edit"/>
              <dialogModal v-if="checked.length>0" message="Are you sure you want to delete these users?"
                           invokeButtonIcon="el-icon-delete" @accepted="emitDeleteAccepted"/>
              </span>
            </el-pagination>
      </span>
    </div>
</template>

<script>
    import DialogModal from './DialogModal.vue'

    export default {
        data() {
            return {
                checked: [],
                currentCell: Object,
                changedCells: []
            }
        },
        components: {
            DialogModal
        },
        props: {
            tableData: {
                type: Array,
                required: true
            },
            tableColumns: {
                type: Array,
                required: true
            }
        },
        methods: {
            handleCellFocus(event) {
                if (event.target !== this.currentCell) {
                    this.currentCell = event.target;
                }
            },
            handleCellChange(index) {
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
                if (row[this.tableColumns[0].colname]) {
                    this.emitEdit(index, row);
                }
                else {
                    this.emitCreate(index, row);
                }
            },
            handleDelete(index, row) {
                this.$emit('delete-executed', index, row);
            },
            handleSelectionChange(val) {
                this.checked = val;
            },
            emitDeleteAccepted() {
                this.$emit('delete-accepted');
            },
            emitEdit(index, row) {
                this.resetChangedCellsStyleClass(index);

                this.manageCurrentRowEditButtonStyleClasses(index, "component-display-visible", "component-display-nonvisible");

                this.$emit('edit-executed', index, row);
            },
            emitCreate(index, row) {
                this.resetChangedCellsStyleClass(index);

                this.manageCurrentRowEditButtonStyleClasses(index, "component-display-visible", "component-display-nonvisible");

                this.$emit('create-executed', index, row);
            },
            addNew() {
                let columnNames = this.tableColumns.map(function (tableColumnElement) {
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
            resetChangedCellsStyleClass(index) {
                this.changedCells.forEach(function (tableCell) {
                    if (tableCell.name.endsWith(index)){
                        tableCell.className = tableCell.className.replace("changed-input-text", "");
                    }
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