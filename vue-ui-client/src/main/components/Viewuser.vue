<template>
    <div>
        <p>user {{this.$route.params.userid}}</p>
        <router-link :to="'/all_users'" tag="button">All users</router-link>
        <delete message="Are you sure you want to delete the user?"
                @delete-accepted="deleteAccepted"
                @delete-canceleted="deleteCanceled"></delete>
    </div>
</template>
<script>
    import Delete from './DialogModal.vue'
    import config from '../js/index.js'
    export default {
        components: {
            'delete': Delete
        },
        methods: {
            'deleteAccepted': function () {
                config.router.push('/all_users')
            },
            'deleteCanceled': function () {

            }
        }
    }
</script>
