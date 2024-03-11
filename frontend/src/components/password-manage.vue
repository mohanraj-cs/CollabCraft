<template>
<div>
     <backgroundimg></backgroundimg>
     <div id="change-form">
         <el-button type="text" icon="el-icon-arrow-left" id="back" @click="toDocumentManage">Back</el-button>
         <el-form :model="form" label-position="right" ref="passwordForm" size="mini" :rules="rule" label-width="80px">
             <el-form-item label="old password" prop="oldPassword">
                 <el-input type="password" v-model="form.oldPassword" size="mini"></el-input>
             </el-form-item>
             <el-form-item label="New Password" prop="newPassword">
                 <el-input type="password" v-model="form.newPassword" size="mini"></el-input>
             </el-form-item>
             <el-form-item label="Confirm Password" prop="confirmPassword">
                 <el-input type="password" v-model="form.confirmPassword" size="mini"></el-input>
             </el-form-item>
             <el-form-item>
                 <el-button type="primary" round @click="changePassword" style="width:100px;margin-top:10px;">Confirm changes</el-button>
             </el-form-item>
         </el-form>`
     </div>
</div>
</template>

<script>
import backgroundimg from './background-img'
export default {
     data() {
         var validateOldPassword = (rule,value,callback) => {
             if(!value)
             {
                 callback (new Error('Password cannot be empty'))
             } else
             {
                 callback()
             }
         };
         var validateNewPassword = (rule,value,callback) => {
             if(!value)
             {
                 callback (new Error('Password cannot be empty'))
             }
             console.log(value)
             console.log(this.form.oldPassword)
             if(value === this.form.oldPassword)
             {
                 callback (new Error('cannot be consistent with the old password'))
             } else
             {
                 callback()
             }
         };
         var validateConfirmPassword = (rule,value,callback) => {
             if(!value)
             {
                 return callback(new Error('Confirm password cannot be empty'))
             }
             if(value !== this.form.newPassword)
             {
                 callback(new Error('Two passwords are inconsistent'))
             } else
             {
                 callback()
             }
         };
         return {
             form: {
                 oldPassword: '',
                 newPassword: '',
                 confirmPassword: ''
             },
             rule: {
                 oldPassword: [{validator: validateOldPassword, trigger: 'blur'}],
                 newPassword: [
                     {validator: validateNewPassword, trigger: 'blur'},
                     {min: 6, message: 'Password must be at least 6 characters', trigger: 'blur'}
                 ],
                 confirmPassword: [{validator: validateConfirmPassword, trigger: 'blur'}],
             }
         }
     },
     components: {backgroundimg},
     methods: {
         changePassword() {
             this.$refs['passwordForm'].validate((valid) => {
                 if(valid)
                 {
                     this.$axios(
                     {
                         url:'/modifyKey',
                         method:"post",
                         data:{
                             username: window.sessionStorage.username,
                             oldPassword: this.form.oldPassword,
                             newPassword: this.form.newPassword
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
                     })
                     .then(response => {
                         if(response.data.message === 'success')
                         {
                             this.$message({
                                 message: 'Modification successful',
                                 type: 'success'
                             })
                             this.$router.push({
                                 path: '/document-manage'
                             })
                         } else if(response.data.message === 'fail')
                         {
                             this.$message({
                                 message: 'Wrong password, please try again',
                                 type: 'error'
                             })
                         }
                     });
                 }
             })
            
         },
         toDocumentManage() {
             this.$router.push({
                 path: '/document-manage'
             })
         }
     }
}
</script>

<style scoped>
@import '../../static/css/keyframe.css';
#change-form {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%,-50%);
    width: 400px;
    background-color: rgba(245,245,245,0.6);
    border-radius: 5px;
    opacity: 0;
    animation: fadeIn 1s ease 0.5s 1;
    animation-fill-mode: forwards;
}
#back {
    position: absolute;
    left: 5%;
    top: 5%;
}
.el-form {
    width: 250px;
    position: relative;
    margin-top: 70px;
    margin-left: 17%;
}
</style>