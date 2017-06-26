<template>
<p>user {{this.$route.params.userid}}</p>
</template>
<script>
export default{
	watch: {
	    '$route' (to, from) {
	    	
	    }
	  }
}
</script>