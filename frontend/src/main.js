import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
import ElementUI from 'element-ui'
import VueQuillEditor from 'vue-quill-editor'
import VueClipboard from 'vue-clipboard2';
import store from './store'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'
import 'element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false
Vue.prototype.$axios = axios
axios.defaults.baseURL = process.env.BASE_API;
Vue.use(ElementUI)
Vue.use(VueQuillEditor, /* { default global options } */)
Vue.use(VueClipboard)
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
