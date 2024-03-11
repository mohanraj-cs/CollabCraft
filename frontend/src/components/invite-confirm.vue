<template>
     <div>
         <backgroundimg></backgroundimg>
         <div class="header" align="center">
             CollabCraft
         </div>
         <div class="main" align="center">
             <el-form ref="login" :model="login" style="width:250px;margin-top:20px;">
                 <el-form-item>
                     <el-input v-model="login.username" placeholder="username" required=true></el-input>
                 </el-form-item>
                 <el-form-item>
                     <el-input v-model="login.password" placeholder="password" type="password" required=true></el-input>
                 </el-form-item>
                 <el-form-item>
                     <el-button type="primary" style="width:150px;margin-top:15px;" @click="submitForm">Login</el-button>
                 </el-form-item>
             </el-form>
         </div>
     </div>
</template>

<script>
import backgroundimg from './background-img'
export default {
     name: 'invite-confirm',
     data() {
         return {
             login: {
                 username: '',
                 password: ''
             }
         }
     },
     mounted() {
         (document.getElementById('loading')).style.display = "none"
         //The message returns true and pops up the message. If the message returns false, it becomes 404.
         this.$axios(
         {
             url:'/invite/checkDocId',
             method:"post",
             data:{
                 docID: this.$route.params.docId
             },
             transformRequest: [function (data) {
             // Do whatever you want to transform the data
             let ret = ''
             for (let it in data) {
             // If you want to send Chinese encoding
                 ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
             }
             return ret
             }],
             headers: {
                 'Content-Type':'application/x-www-form-urlencoded'
             }
         }).catch(error => {
             console.log(error.message);
             //If an error is reported, also jump to 404
             this.$router.push({
                 path: '/404'
             })
         })
         .then(response => {
             console.log(response.data.message)
             if(response.data.message === 'success')
             {
                 this.$message({
                     message: "Please log in to accept the invitation",
                     type: "success",
                     duration: 0,
                     showClose: true
                 })
                  //If the current page jumps, clear the previous user information first
                 window.sessionStorage.removeItem('username')
                 window.sessionStorage.removeItem('email')
             } else
             {
                 this.$router.push({
                     path: '/404'
                 })
             }
         });
     },
     methods: {
         submitForm() {
             this.$axios(
             {
                 url:'/invite/checkUser',
                 method:"post",
                 data:{
                     username: this.login.username,
                     password: this.login.password,
                     docID: this.$route.params.docId,
                     auth: this.$route.params.auth
                 },
                 transformRequest: [function (data) {
                 // Do whatever you want to transform the data
                 let ret = ''
                 for (let it in data) {
                 // If you want to send Chinese encoding
                     ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
                 }
                 return ret
                 }],
                 headers: {
                     'Content-Type':'application/x-www-form-urlencoded'
                 }
             }).catch(error => {
                 console.log(error.message);
                 //If an error is reported, also jump to 404
                 this.$router.push({
                     path: '/404'
                 })
             })
             .then(response => {
                 if(response.data.message === 'userError')
                 {
                     this.$message({
                         message: 'Username or password is incorrect',
                         type: 'error',
                         duration: 2000
                     })
                 }
                 if(response.data.message === 'docError')
                 {
                     this.$message({
                         message: 'There is no sharing information for this document',
                         type: 'error',
                         duration: 2000
                     })
                 }
                 if(response.data.message === 'authError')
                 {
                     this.$message({
                         message: 'You already have permission for this document',
                         duration: 2000
                     })
                 }
                 if(response.data.message === 'success')
                 {
                     window.sessionStorage.setItem('username',this.login.username)
                     window.sessionStorage.setItem('email', response.data.email)
                     this.$router.push({
                         path: '/document-manage'
                     })
                 }
             });
         },
        handleKeyDown(e) {
             var that = this
             var key = window.event.keyCode ? window.event.keyCode : window.event.which
             if(key === 13){
                 if(flag)
                 {
                     that.submitForm()
                     flag = false
                 }
                 e.preventDefault()
             }
         },
         handleKeyUp(e) {
             var that = this
             // enter
             var key = window.event.keyCode ? window.event.keyCode : window.event.which
             if(key === 13){
                 flag = true
                 e.preventDefault()
             }
         },
         created() {
             document.addEventListener('keydown', this.handleKeyDown)
             document.addEventListener('keyup', this.handleKeyUp)
         },
         destroyed() {
             document.removeEventListener('keydown', this.handleKeyDown)
             document.removeEventListener('keyup', this.handleKeyUp)
         },
     },
     components: { backgroundimg }
    
}
</script>

<style scoped>
@import '../../static/css/keyframe.css';
.header {
     position: absolute;
     font-size:40px;
     font-weight: bold;
     left: 50%;
     top: 25%;
     opacity: 0;
     transform: translate(-50%, -25%);
     animation: fadeIn 2s ease 0.5s 1;
     animation-fill-mode: forwards;
}
.main {
     position: absolute;
     left: 50%;
     top: 45%;
     opacity: 0;
     transform: translate(-50%, -30%);
     animation: fadeIn 2s ease 1s 1;
     animation-fill-mode: forwards;

}
</style>