<template>
<div>
    <navmenu></navmenu>
    <div id="editor">
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
    </div>
</div>
</template>

<script>
import navmenu from './nav-menu'
import { CurentTime } from '../../static/js/DiffToStringArray'
export default {
    name: 'document-edit',
    data() {
        return {
            docName: '',
            docID: '',
            docOwner: '',
            content: '',
            userString: '',
            editorOption: { 
                placeholder: "Enter anything，support html",
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
            timer: ''
        }
    },
    components: {navmenu},
    methods: {
        toDocumentManage() {
            this.$store.commit('removeFilePath')
            this.$router.push({
                path: '/document-manage'
            })
        },
        changeData(response_content)
        {
            this.$refs.myQuillEditor.quill.deleteText(0,this.content.length)
            this.$refs.myQuillEditor.quill.insertText(0,response_content)
            content = this.content
            new_content = this.content
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
            this.$refs.myQuillEditor.quill.enable(false)
            this.initWebSocket()
        });
    }
}
</script>

<style scoped>
#editor{
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
.quill-editor {
    position:absolute;
    left: 25%;
    top: 15%;
    min-height: 200px;
    height: 400px;
    width: 900px;
    min-width: 900px;
}
</style>