<template>
<div>
     <div id="menu">
         <div id="app_title">CollabCraft</div>
         <el-autocomplete
             placeholder="Please enter the document name"
             v-model="searchValue"
             size="medium"
             :fetch-suggestions="querySearchAsync"
             id="searchInput"
             @select="handleSelect"
             clearable
             style="position: absolute; width:20%;right:10%; top:15px;">
             <i slot="prefix" class="el-input__icon el-icon-search"></i>
             <template slot-scope="props">
                 <div>
                     <span class="iconfont icon-txt-ext" style="font-size:15px;"></span>
                     <span class="docName">{{ props.item.docName}}</span>
                     <div class="auth">{{ props.item.auth }}</div>
                     <div class="time">{{ props.item.lastModifyTime }}</div>
                 </div>
             </template>
         </el-autocomplete>
         <div id="my">
             <el-dropdown size="medium" @command="handleCommand">
                 <span class="iconfont icon-user" style="font-size:25px;"></span>
                 <el-dropdown-menu slot="dropdown">
                 <el-dropdown-item>
                     Welcome back, {{username}}
                     <div style="font-size: 10px;color: #b4b4b4; margin-top: -8px;">Bind email: {{email}}</div>
                 </el-dropdown-item>
                 <el-dropdown-item divided command="modify">Modify password</el-dropdown-item>
                 <el-dropdown-item divided command="logout">Logout</el-dropdown-item>
                 </el-dropdown-menu>
             </el-dropdown>
         </div>
     </div>
</div>
</template>

<script>
import '../assets/myicon/iconfont.css'
import { convertTimeFormat } from '../../static/js/convertTime'
export default {
     inject: ['reload'],
     name: 'nav-menu',
     data() {
         return {
             username: window.sessionStorage.username,
             email: window.sessionStorage.email,
             searchValue: '',
             docList: [],
             show: false
         }
     },
     methods: {
         handleCommand(command) {
             if(command === 'logout')
             {
                 window.sessionStorage.removeItem('username')
                 window.sessionStorage.removeItem('email')
                 this.$router.push({
                     path: '/'
                 })
             }
             if(command === 'modify')
             {
                 this.$router.push({
                     path: '/password-manage'
                 })
             }
         },
         //queryString is the value entered in the box
         //callback callback function, push the processed data back
         querySearchAsync(queryString, callback) {
             var list = [];
             queryString = this.searchValue
             //Get the object array from the background
             this.$axios(
             {
                 url:'/selectDoc',
                 method:"post",
                 data:{
                     keyword: queryString,
                     username: window.sessionStorage.username
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
                 for(var i = this.docList.length-1; i >= 0; i--)
                 {
                     this.$delete(this.docList, i)
                 }
                 for(var i = 0; i < response.data.docList.length; i++)
                 {
                     var doc = response.data.docList[i]
                     this.docList.push({docName: 'Document:' + doc.docName, auth: 'Permission:' + doc.auth, lastModifyTime: 'Last modification time:' + convertTimeFormat(doc.lastModifyTime), oldPath: doc.oldPath , newPath: doc.newPath})
                 }
                 callback(this.docList)
             })
         },
        handleSelect(item) {
             console.log(item)
             this.$store.commit('removeFilePath')
             this.$store.commit('setFilePath', {oldPath: item.oldPath, newPath: item.newPath})
             if(item.auth === 'read')
             {
                 if(this.$route.path === '/document-explore') //If it is on the current page, just refresh it
                 {
                     this.reload()
                 } else
                 {
                     this.$router.push({
                         path: '/document-explore'
                     })
                 }
             } else
             {
                 if(this.$route.path === '/document-edit')
                 {
                     this.reload()
                 } else
                 {
                     this.$router.push({
                         path: '/document-edit'
                     })
                 }
             }
            
         }
     }
}
</script>

<style scoped>
body {
     position: absolute;
}
.docName {
     text-overflow: ellipsis;
     overflow: hidden;
}
.auth {
     font-size: 12px;
     color: #b4b4b4;
     margin-top: -10px;
}
.time {
     font-size: 10px;
     color: #b4b4b4;
     margin-top: -15px;
}
#app_title {
     position: absolute;
     top: 15px;
     left: 30px;
     font-size: 25px;
}
#my {
     position: absolute;
     left: 95%;
     top: 20px;
}
#menu {
     position: absolute;
     left: 0;
     top: 0;
     background-color: white;
     height: 60px;
     width: 100%;
     min-width: 1300px;
}

</style>