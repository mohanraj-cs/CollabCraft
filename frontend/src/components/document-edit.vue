<template>
<div>
    <navmenu></navmenu>
    <div id="editor">
        <div id="history-menu" @mouseenter="showChange" @mouseleave="showChange" v-bind:class="{ visibleMenu: show, hiddenMenu:!show }" align="center">
            <span v-show="!showItem" class="iconfont icon-right-circle" style="position:absolute;right:5%;top:45%;font-size:30px;"></span>
            <span v-show="showItem" class="iconfont icon-left-circle" style="position:absolute;right:5%;top:45%;font-size:30px;"></span>
            <div v-bind:class="{ visibleItem: showItem, hiddenItem:!showItem }">
                <span style="margin-top:10px;display:inline-block">Current historical version：</span>
                <div class="history-item" v-for="(val,index) in historyOptions" ref="block" v-bind:key="'historyItem'+index" style="margin-top:20px;">
                    <span style="font-size:15px;">{{val.timestamp}}</span>
                    <br/>
                    <el-button type="text" style="color:black" @click="toDocumentHistory(index)">Check <span class="iconfont icon-lookup" style="font-size:15px;"></span></el-button>
                </div>
            </div>
        </div>
        <div id="title-group" align="left">
            <el-button type="text" icon="el-icon-arrow-left" style="font-size:20px;color:black;margin-left:20px;" @click="toDocumentManage">return</el-button>
            <el-tag style="margin-left:30px;">document：{{docName}}</el-tag>
            <el-tag type="success" style="margin-left:10px;">Document owner：{{docOwner}}</el-tag>
            <el-tag type="warning" style="margin-left:10px;">Shared members：{{userString}}</el-tag>
        </div>
        <quill-editor
            v-model="content"
            ref="myQuillEditor"
            :options="editorOption">
        </quill-editor>
        <div id="button-group" align="right">
            <el-button type="success" @click="saveFile" size="medium">save<span class="iconfont icon-save" style="font-size:10px;margin-left:5px;"></span></el-button>
            <el-button type="primary" @click="showShareDialog" size="medium">share<span class="iconfont icon-share" style="font-size:10px;margin-left:5px;"></span></el-button>
        </div>
    </div>
    <el-dialog
        title="Invite members"
        :visible.sync="shareDialogVisible"
        width="700px"
        @close="closeShareDialog">
        <div id="shareType" style="margin-bottom:10px;">
          Please select the type of permissions you want to share
            <el-radio-group v-model="shareType" size="small" style="margin-left:20px;">
                <el-radio-button label="Edit"></el-radio-button>
                <el-radio-button label="Check"></el-radio-button>
            </el-radio-group>     
        </div>
        <div id="userInputList" style="margin-top:25px;">
            <div v-for="(item,i) of userList" v-bind:key="'user'+i" style="margin-bottom:10px;margin-top:15px;">
                <el-select v-model="userList[i]" filterable placeholder="please choose">
                    <el-option
                    v-for="item in userOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                    </el-option>
                </el-select>
                  <el-button type="danger" @click="deleteUser(i)" size="medium" style="margin-left:25px;">delete</el-button>
            </div>
        </div>
        <el-button type="primary" @click="addNewUser" size="medium" style="margin-top:10px;">New</el-button>
        <span slot="footer" class="dialog-footer">
            <el-button type="success" @click="shareFile">Generate link</el-button>
        </span>
    </el-dialog>
</div>
</template>

<script>
import '../assets/myicon/iconfont.css'
import navmenu from './nav-menu'
import { diff_match_patch } from '../../static/js/diff_match_patch'
import { update,CurentTime,replaceAll } from '../../static/js/DiffToStringArray'
import { setTimeout, clearTimeout, setInterval, clearInterval } from 'timers';
var content, new_content
var pos
var flag = true
export default {
    name: 'document-edit',
    data() {
        return {
            websock: null,
            docName: '',
            docID: '',
            docOwner: '',
            userString: '',
            content: '',
            editorOption: { 
                placeholder: "Enter any content，support html",
                modules: {  
                    toolbar: [
                    ['bold', 'italic', 'underline', 'strike'],
                    ['blockquote', 'code-block'],
                    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                    [{ 'indent': '-1' }, { 'indent': '+1' }],
                    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
                    [{ 'font': [] }],
                    [{ 'color': [] }, { 'background': [] }],
                    [{ 'align': [] }],
                    ]
                }
            },
            timer: '',
            show: false,
            showItem: false,
            shareDialogVisible: false,
            shareType: 'edit',
            shareLink: '',
            userList: [],
            userOptions: [],
            historyOptions: []
        }
    },
    components: {navmenu},
    methods: {
        showChange() {
            this.show = !this.show
            this.showItem = !this.showItem
            this.$axios(
            {
                url:'/version-manage/show',
                method:"post",
                data:{
                    username: window.sessionStorage.username,
                    filepath: this.$store.state.doc.oldPath
                },
                transformRequest: [function (data) {
                let ret = ''
                for (let it in data) {
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
            .then(async response => {
                await this.changeHistoryOptions(response.data.versionItem)
                for(var i = 0; i < this.historyOptions.length; i++)
                {
                    this.$refs['block'][i].style.backgroundColor = this.historyOptions[i].color
                }
            });
        },
        async changeHistoryOptions(versionItem) 
        {
            for(var i = this.historyOptions.length-1; i >= 0; i--)
            {
                this.$delete(this.historyOptions,i)
            }
            for(var time in versionItem)
            {
                this.historyOptions.push({timestamp:time,color:versionItem[time]})
            }
        },
        toDocumentManage() {
            this.$router.push({
                path: '/document-manage'
            })
        },
        toDocumentHistory(index) {
            this.$router.push({
                path: '/document-history',
                query: {
                    timeStamp: this.historyOptions[index].timestamp,
                    docName: this.docName
                }
            })
        },
        timeUpdate() {
            pos = this.$refs.myQuillEditor.quill.selection.savedRange.index
            new_content = this.content
            console.log('When comparing:')
            console.log('content:',content)
            console.log('new_content:',new_content)
            var dmp = new diff_match_patch();
            var diff = dmp.diff_main(replaceAll(content,'<br>'),replaceAll(new_content,'<br>'));
            var opList = (update(diff))
            console.log(opList)
            console.log("diff:",diff)
            if(opList)
            {
                console.log('Communicate with the backend')
                let data = {"opList":opList,"newPath": this.$store.state.doc.newPath,"oldPath": this.$store.state.doc.oldPath,"username": window.sessionStorage.username,"timeStamp": CurentTime()}
                this.websocketsend(JSON.stringify(data))
            }
        },
        changeData(response_content)
        {
            this.$refs.myQuillEditor.quill.deleteText(0,this.content.length)
            this.$refs.myQuillEditor.quill.insertText(0,response_content)
            content = this.content
            new_content = this.content
        },
        saveFile() {
            this.$axios(
            {
                url:'/version-manage/add',
                method:"post",
                data:{
                    username: window.sessionStorage.username,
                    filepath: this.$store.state.doc.oldPath
                },
                transformRequest: [function (data) {
                let ret = ''
                for (let it in data) {
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
                        message: 'Saved successfully',
                        type: 'success',
                        duration: 2000
                    }) 
                } else if(response.data.message === 'fail')
                {
                    this.$message({
                        message: 'Save failed',
                        type: 'error',
                        duration: 2000
                    })
                }
            })
        },
        showShareDialog() {
            for(var i = this.userList.length-1; i >= 0; i--)
            {
                this.$delete(this.userList,i)
            }
            this.userList.push('')
            this.$axios(
            {
                url:'/share/send',
                method:"post",
                data:{
                    username: window.sessionStorage.username,
                    docName: this.docName + '.txt'
                },
                transformRequest: [function (data) {
                let ret = ''
                for (let it in data) {
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
                for(var i = 0; i < response.data.usernameList.length; i++)
                {
                    this.userOptions.push({label:response.data.usernameList[i],value:response.data.usernameList[i]})
                }
                return (response)
            });
            this.shareDialogVisible = true
        },
        closeShareDialog() {
            for(var i = this.userOptions.length-1; i >= 0; i--)
            {
                this.$delete(this.userOptions,i)
            }
        },
        addNewUser() {
            this.userList.push('')
        },
        deleteUser(index) {
            this.$delete(this.userList,index)
        },
        shareFile() {
            var auth = ''
            if(this.shareType === 'edit')
            {
                auth = 'share'
            } else if(this.shareType === 'Check')
            {
                auth = 'read'
            }
            var templist = this.userList.sort()
            var duplicateFlag = false
            for(var i = 0; i < templist.length - 1; i++)
            {
                if(templist[i] === templist[i+1])
                {
                    duplicateFlag = true
                }
            }
            if(duplicateFlag)
            {
                this.$message.error('Don\'t invite the same person twice')
                return 
            }
            this.$axios(
            {
                url:'/shareInfo',
                method:"post",
                data:{
                    userList: this.userList,
                    authority: auth,
                    filePath: this.$store.state.doc.oldPath
                },
                transformRequest: [function (data) {
                let ret = ''
                for (let it in data) {
                    ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
                }
                return ret
                }],
                headers: {
                    'Content-Type':'application/x-www-form-urlencoded'
                }
            }).catch(error => {
                console.log(error.message);
                this.$message.error('Build failed')
            })
            .then(response => {
                this.shareLink = 'http://localhost:8082/#/inviteConfirm/' + this.docID + '/' + auth
                this.$copyText(this.shareLink).then(()=> {
                this.$message({
                    message: 'Generate link successfully，Copied to your clipboard',
                    type: 'success'
                })
                this.$confirm('Do you want to send invitation emails to inviting users?', 'hint', {
                    confirmButtonText: 'Sure',
                    cancelButtonText: 'Cancel',
                    type: 'warning'
                }).then(() => {
                    this.$axios(
                    {
                        url:'/mail/send-invite',
                        method:"post",
                        data:{
                            username: window.sessionStorage.username,
                            invitelist: this.userList,
                            invitelink: this.shareLink,
                            privilege: auth,
                        },
                        transformRequest: [function (data) {
                        let ret = ''
                        for (let it in data) {
                            ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
                        }
                        return ret
                        }],
                        headers: {
                            'Content-Type':'application/x-www-form-urlencoded'
                        }
                    }).catch(error => {
                        console.log(error.message);
                        this.$message.error('Build failed')
                    })
                    .then(response => {
                        if(response.data.message === 'success')
                        {
                            this.$message({
                                message: 'Email sent successfully\n',
                                type: 'success'
                            })
                        } else if(response.data.message === 'fail')
                        {
                            this.$message({
                                message: 'Failed to send',
                                type: 'error'
                            })
                        }
                    });

                }).catch(() => {        
                });
                },()=> {
                })
            });
            this.shareDialogVisible = false
        },
        initWebSocket(){
            const wsuri = "ws://localhost:8080/socketServer?username=" + window.sessionStorage.username + "&docName=" + this.docName + ".txt" + "&docOwner=" + this.docOwner;
            this.websock = new WebSocket(wsuri);
            this.websock.onmessage = this.websocketonmessage;
            this.websock.onopen = this.websocketonopen;
            this.websock.onerror = this.websocketonerror;
            this.websock.onclose = this.websocketclose;
        },
        websocketonopen(){
        },
        websocketonerror(){
            this.initWebSocket();
        },
        websocketonmessage(e){
            var data = JSON.parse(e.data)
            console.log(data.content)
            this.changeData(data.content)
            this.$refs.myQuillEditor.quill.setSelection(pos)
        },
        websocketsend(Data){
            this.websock.send(Data);
        },
        websocketclose(e){
            console.log('Disconnect',e);
        },
        handleKeyDown() {
            // ctrl + s
            var key = window.event.keyCode ? window.event.keyCode : window.event.which
            if( window.event.ctrlKey && key === 83 ){
                if(flag)
                {
                    this.saveFile()
                    flag = false
                }
                window.event.preventDefault()
            }
        },
        handleKeyUp() {
            var key = window.event.keyCode ? window.event.keyCode : window.event.which
            if( window.event.ctrlKey && key === 83 ){
                flag = true
                window.event.preventDefault()
            }
        }
    },
    created() {
        document.addEventListener('keydown', this.handleKeyDown);
        document.addEventListener('keyup',this.handleKeyUp);
    },
    mounted() {
        (document.getElementById('loading')).style.display = "none"
        this.$axios(
        {
            url:'/edit',
            method:"post",
            data:{
                oldPath: this.$store.state.doc.oldPath,
                newPath: this.$store.state.doc.newPath
            },
            transformRequest: [function (data) {
            let ret = ''
            for (let it in data) {
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
            this.$refs.myQuillEditor.quill.insertText(0,response.data.content)
            content = this.content
            pos = 0
            this.docName = response.data.docName.replace('.txt','')
            this.docID = response.data.docID
            this.docOwner = response.data.docOwner
            var userString = ''
            for(var i = 0; i < response.data.docSharer.length; i++)
            {
                userString = userString + response.data.docSharer[i] + ' '
            }
            if(userString === '')
            {
                this.userString = 'none'
            } else
            {
                this.userString = userString
            }
            this.initWebSocket()
            this.timer = setInterval(this.timeUpdate, 3000)
        });
    },
    beforeDestroy() {
        clearInterval(this.timer)
    },
    destroyed() {
        this.websock.close()
        document.removeEventListener('keydown', this.handleKeyDown)
        document.removeEventListener('keyup', this.handleKeyUp)
    },
}
</script>

<style scoped>
body {
    position: absolute;
}
.visibleMenu {

    width: 300px;
    z-index: 2;
    background-color: white;
    transition: width, background-color, z-index;
    transition-duration: 0.5s;
}
.hiddenMenu {
    width: 70px;
    background-color: #F7F7F7;
    z-index: 0;
    transition: width, background-color, z-index;
    transition-duration: 0.5s;
}
.visibleItem {
    opacity: 1;
    transition: opacity;
    transition-delay: 0.3s;
    transition-duration: 1s;
}
.hiddenItem {
    opacity: 0;
    transition: opacity;
    transition-duration: 0.1s;
}
.history-item {
    width: 200px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
}
#history-menu {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    overflow: auto;
}
#editor {
    position: absolute;
    left: 0;
    top: 60px;
    bottom: 0;
    right: 0;
    background-color: #F7F7F7;
}
#title-group {
    position: relative;
    top: 20px;
    left: 20px;
}
#button-group {
    position: relative;
    top: 20px;
    right: 5%;
}
.quill-editor {
    position:absolute;
    left: 20%;
    top: 20%;
    min-height: 200px;
    height: 400px;
    width: 900px;
    min-width: 900px;
}
.errorMessage {
    position: absolute;
    color:red;
    left:10%;
}
</style>