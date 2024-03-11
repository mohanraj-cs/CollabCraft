<template>
<div>
     <h2>Sorry, the address you entered was not found</h2>
     <h3>o(╥﹏╥)o</h3>
     <el-button type="text" @click="goLogin">Click to return to the homepage</el-button>

</div>
</template>

<script>
export default {
     name: 'notfound',
     data() {
         return {

         }
     },
     methods: {
         goLogin() {
             this.$router.push({
                 path: '/'
             })
         }
     },
     mounted() {
         (document.getElementById('loading')).style.display = "none"
     }
}
</script>
<style scoped>

</style>