
<template>
  <main>
    欢迎欢迎

    <div style="margin: 20px">
      <el-button type="info" @click="router.back()">返回</el-button>
    </div>



    <div style = "font-size:30px">
      {{ a }}<br>
      {{data.a}}
      {{data.b}}
    </div>

    <div>
      <!--双向绑定-->
      <input v-model="data.a"/>
    </div>

    <!--if语句-->
    <div>
      <span style="color: brown" v-if="data.a === '小猪佩奇'">小猪佩奇</span>
      <span style="color: darkorange" v-if="data.a === '小猪乔治'">小猪乔治</span>
      <span style="color: deeppink" v-else-if="data.a === '小猪朵朵'">小猪朵朵</span>
    </div>

    <!--for循环-->
    <div style="display: flex">
    <div @click="show(item)" style="width: 300px; height:300px; text-align: center;line-height: 300px;font-size: 30px; margin-right: 10px;background-color: greenyellow" v-for="item in data.arr">{{item}}</div>
    </div>

    <div style="margin-bottom: 20px">
      <select style="width: 200px">
        <option v-for="item in data.fruit">{{item}}</option>
      </select>
    </div>

    <div style="margin-bottom: 20px">
<!--点击事件-->
      <button v-on:click="click">点我加好运</button>
      <button @click="click">这也是好运</button>
    </div>

    <!--v-bind的style绑定-->
    <div v-bind:style="data.vbindbox"></div>

    <!--有冒号，得用‘’裹上字符串-->
    <div style="margin-bottom: 20px">
      <img :src="'https://www.runoob.com/wp-content/uploads/2017/01/vue.png'" alt="">
    </div>

    <!--element-plus组件按钮使用-->
    <div style="margin-bottom: 20px">
       <el-button @click="click" color="#626aef" :dark="isDark" style="color:greenyellow">Default</el-button>
    </div>

    <!--icon使用-->
    <div>
      <span>
        <!--top:在图标上方加空隙-->
        <el-icon :size="50" color="#666" style="top: 5px"><Compass /></el-icon>100
      </span>

      <el-button type="danger" :icon="Delete" circle />

      <el-input
      v-model="input2"
      style="width: 240px"
      placeholder="Type something"
      :prefix-icon="Search"
    />
    </div>

     <div class="mb-4" style="margin: 30px">
    <el-button>Default</el-button>
    <el-button type="primary">Primary</el-button>
    <el-button type="success">Success</el-button>
    <el-button type="info">Info</el-button>
    <el-button type="warning">Warning</el-button>
    <el-button type="danger">Danger</el-button>
  </div>


  </main>
</template>


<script setup>
import {reactive, ref,onMounted} from "vue";
import {Compass, Delete, Search} from "@element-plus/icons-vue";
import router from "@/router/index.js";
// 数据绑定
const a = ref(1)
const input2 = ref('')
const data = reactive({
  a:"小猪乔治",
  b:"你好",
  arr:[1,2,3],
  fruit:['apple','banana','orange'],
  vbindbox:{width:'100px',height:'100px',backgroundColor:'red'}
    }
)

onMounted(()=>{
  alert("页面加载完成")
})
// 点击函数
const click=()=>{
  alert("好运+1")
}

const show=(value)=>{
  alert(value)
}
</script>
