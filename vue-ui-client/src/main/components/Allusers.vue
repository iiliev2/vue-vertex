<template>
    <div>
        <table id="allusers">
            <tr v-for="item in items">
                <td>
                    <input type="checkbox" v-bind:value="item.id" v-model="checked">
                </td>
                <div v-on:click="viewUser(item)">
                    <td v-for="value in item">{{value}}</td>
                </div>
            </tr>
        </table>

        <router-link v-bind:to="'/create_user'" tag="button">Create User</router-link>
        <delete v-if="checked.length>0"
                message="Are you sure you want to delete these users?"
                @delete-accepted="deleteAccepted"
                @delete-canceleted="deleteCanceled">
        </delete>
    </div>
</template>

<script>
    import {
        router
    } from '../js/index.js'
    import Delete from './Delete.vue'
    export default {
        data() {
            return {
                items: [{
                    id: 1,
                    name: 'Ivan Iliev'
                },
                    {
                        id: 2,
                        name: 'Pesho Typiq'
                    }
                ],
                viewUser(user) {
                    router.push('/user/:' + user.id)
                },
                checked: []
            }
        }, components: {
            'delete': Delete
        },
        methods: {
            'deleteAccepted': function () {

            },
            'deleteCanceled': function () {

            }
        }
    }
</script>
<style scoped>
    #allusers {
        background: limegreen;
        margin: 0px 0px 20px 0px;
    }
</style>
