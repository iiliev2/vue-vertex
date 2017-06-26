import Vue from 'vue'
import VueRouter from 'vue-router'
import VueResource from 'vue-resource'
import App from './../components/App.vue'
import Allusers from './../components/Allusers.vue'
import Createuser from './../components/Createuser.vue'
import Deleteuser from './../components/Deleteuser.vue'
import Viewuser from './../components/Viewuser.vue'
Vue.use(VueResource)
Vue.use(VueRouter)

const routes = [
	{path: '/all_users', component: Allusers},
	{path: '/create_user', component: Createuser},
	{path: '/user/:userid', component: Viewuser},
	{path: '/delete_user/:userid', component: Deleteuser}	
]

export var router = new VueRouter({
	routes,

})

new Vue({
	el: '#app',
	render: h=>h(App),
	router
	})

router.push('/all_users')