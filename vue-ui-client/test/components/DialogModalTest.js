import Vue from 'vue';
import test from 'ava';
import ElementUI from 'element-ui';
import DialogModal from './../../src/main/components/DialogModal.vue';

Vue.use(ElementUI);

let vm;

test.beforeEach(t => {
    let N = Vue.extend(DialogModal);

    vm = new N({ propsData: {
        message: 'Are you sure you want to delete these users?',
        invokeButtonIcon : 'el-icon-delete'
    }}).$mount();
});

test('DialogModal component message property is with wrong value!', t => {
    t.is(vm.message, 'Are you sure you want to delete these users?');
});

