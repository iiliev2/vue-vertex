import Vue from 'vue'
import VueRouter from 'vue-router'
import axios from 'axios'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-default/index.css'
import App from './../components/App.vue'
import Allusers from '../components/BrowseUsers.vue'
import Createuser from './../components/Createuser.vue'
import Viewuser from './../components/Viewuser.vue'
Vue.use(axios)
Vue.use(VueRouter)
Vue.use(ElementUI)

Vue.prototype.$http = axios;

const routes = [{
    path: '/all_users',
    component: Allusers
  },
  {
    path: '/create_user',
    component: Createuser
  },
  {
    path: '/user/:userid',
    component: Viewuser
  }
]

const router = new VueRouter({
  routes
})

const app = new Vue({
  el: '#app',
  render: h => h(App),
  router
})

export default {
  router,
  axios,
  Vue,
  app
}

router.push('/all_users')
