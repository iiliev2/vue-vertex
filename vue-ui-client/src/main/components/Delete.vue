<template>
<span>
        <button id="delete-modal" @click="showDelete = true">{{ buttonLabel }}</button>
        <modal v-if="showDelete">
            <h3 slot="header">Delete</h3>
            <div slot="body">{{ message }}</div>
            <div slot="footer">
                <button class="modal-default-button" @click="clickedOK()">OK</button>
                <button class="modal-default-button" @click="clickedCancel()">Cancel</button>
            </div>
        </modal>
    </span>
</template>

<script>
import Modal from './Modal.vue'
export default {
  data() {
    return {
      showDelete: false
    }
  },
  props: {
    'message': {
      type: String,
      required: true
    },
    'buttonLabel': {
      type: String,
      required: false,
      default: 'Delete'
    }
  },
  components: {
    'modal': Modal
  },
  methods: {
    'clickedOK': function() {
      this.$emit('delete-accepted')
      this.showDelete = false;
    },
    'clickedCancel': function() {
      this.$emit('delete-canceled')
      this.showDelete = false;
    }
  }
}
</script>
<style></style>
