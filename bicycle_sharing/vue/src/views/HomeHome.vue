<template>
  <div>

    <div style="margin: 20px">
      <RouterLink to="/manager/test">跳转到test</RouterLink>
    </div>
<!--附加可返回-->
    <div style="margin: 20px;">
      <el-button type="info" @click="router.push('/manager/test')">push请跳转到此页面</el-button>
    </div>
<!--替换不可返回-->
    <div style="margin: 20px;">
      <el-button type="info" @click="router.replace('/manager/test')">replace请跳转到此页面</el-button>
    </div>

<!--路由传参-->
    <div style="margin: 20px;">
      <el-button type="primary" @click="router.push('/home?id=1&name=于晓璘')">路由传参id=1</el-button>
    </div>

    <div style="margin: 20px;">
      <el-button type="primary" @click="router.push({path:'/home',query:{id:5,name:'于晓璘'}})">路由传参id=1</el-button>
    </div>



    <div style="margin-bottom: 30px">
      <!--必须写v-model-->
      <el-input v-model="data.input" clearable style="width: 240px" placeholder="Please input" :prefix-icon="Search"/>{{data.input}}
      <el-input v-model="data.input" style="width: 240px" placeholder="Please input" :suffix-icon="Calendar"/>{{data.input}}
      <el-input v-model="data.decr" type="textarea" style="width: 300px" placeholder="请输入"></el-input>
    </div>

    <div>
      <el-select
      v-model="data.value"
      value-key="key"
      placeholder="Select"
      size="large"
      style="width: 240px"
    >
        <!--key使用唯一属性：eg:id-->
      <el-option
        v-for="item in data.options"

        :key="item.id"
        :label="item.label"
        :value="item.name"
      />
    </el-select>
    </div>

<!--多选使用multiple属性-->
<div>
      <el-select multiple
      v-model="data.value"
      value-key="key"
      placeholder="Select"
      size="large"
      style="width: 240px"
    >
        <!--key使用唯一属性：eg:id-->
      <el-option
        v-for="item in data.options"

        :key="item.id"
        :label="item.label"
        :value="item.name"
      />
    </el-select>
    </div>

    <div style="margin: 20px">
      <el-radio-group v-model="data.sex">
        <!-- works when >=2.6.0, recommended ✔️ not work when <2.6.0 ❌ -->
        <el-radio value="男">男</el-radio>
        <!-- works when <2.6.0, deprecated act as value when >=3.0.0 -->
        <el-radio value="女">女</el-radio>
      </el-radio-group> <span>{{data.sex}}</span>
    </div>

    <div>
    <el-radio-group v-model="data.buttons" size="large">
      <el-radio-button label="于晓璘" value="于晓璘" />
      <el-radio-button label="大树" value="大树" />
      <el-radio-button label="音乐剧" value="音乐剧" />
      <el-radio-button label="musical" value="musical" />
    </el-radio-group>
  </div>

<!--多选绑定的值是数组-->
    <div style="margin: 20px">
        <el-checkbox-group v-model="data.checkList" :min="1" :max="2">
          <el-checkbox v-for="item in data.options" :key="item.id" :label="item.label" :value="item.name" />
        </el-checkbox-group>
      <span>{{data.checkList}}</span>
    </div>
<!--    image-->
<!--preview-src-list：多张图片预览-->
    <div style="margin: 20px 0">
      <el-image style="width: 100px; height: 100px" :src="img" :fit="fit" :preview-src-list="[img,'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg']"/>
    </div>

    <div style="margin: 20px 0">
      <el-carousel height="300px" style="width: 600px">
      <el-carousel-item v-for="item in data.imgs" :key="item">
        <el-image style="width: 600px; height: 300px" :src="item" :fit="fit" />
      </el-carousel-item>
    </el-carousel>
    </div>


    <div style="margin: 20px">
      <el-date-picker
        v-model="data.date"
        type="date"
        placeholder="Pick a Date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
      />{{data.date}}
      <el-date-picker
        style="margin-left: 20px"
        v-model="data.date1"
        type="datetime"
        placeholder="Pick a Date and Time"
        format="YYYY/MM/DD HH:mm:ss"
        value-format="YYYY-MM-DD HH:mm:ss"
      />{{data.date1}}

      <el-date-picker
        style="margin-left: 20px"
        v-model="data.datarange"
        type="daterange"
        range-separator="To"
        start-placeholder="Start date"
        end-placeholder="End date"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        :size="size"
      />
    </div>{{data.datarange?.length?data.datarange[0]:''}}{{data.datarange?.length?data.datarange[1]:''}}

    <div style="margin: 20px">
      <el-table :data="data.tabledata" stripe style="width: 100%">
      <el-table-column prop="name" label="人名" width="180" />
      <el-table-column prop="sex" label="性别" width="180" />
      <el-table-column prop="country" label="国家" />
        <el-table-column label="操作栏">
<!--          scope:取一整行-->
          <template #default="scope">
            <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
            <el-button type="primary">
              <el-icon  @click="log(scope.row)"><Edit/></el-icon>
            </el-button>
          </template>
        </el-table-column>
  </el-table>
      <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[5,10,15,20]"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="data.tabledata.length"
    />
    </div>


    <div>
      <el-dialog v-model="data.dialogVisible" title="Tips" width="500">
        <div style="margin: 20px">name:{{ data.row.name }}</div>
        <div style="margin: 20px">sex:{{ data.row.sex }}</div>
      </el-dialog>
    </div>

  </div>
</template>

<script setup>
import {reactive} from "vue";
import {Calendar, Edit, Search} from "@element-plus/icons-vue";
import img from '@/assets/logo.svg';
import lun1 from '@/assets/lun1.png';
import lun2 from '@/assets/lun2.png';
import lun3 from '@/assets/lun3.png';
import router from "@/router/index.js";
import request from "@/utils/request.js";

const data = reactive({
  id:router.currentRoute.value.query.id,//获取参数
  name:router.currentRoute.value.query.name,
  input:null,
  derc:'',
  value:'',
  // options:['apple','orange','banana']
  options:[{id:1,label:"apple",name:'apple1'}, {id:2,label:'orange',name:'orange'},{id:3,label:'banana',name: 'banana'},{id:4,label:'apple',name:'apple2'}],
  sex:'',
  buttons:'',
  checkList:[],
  imgs:[lun1,lun2,lun3],
  date:'',
  date1:'',
  datarange:[],
  tabledata:[
    {id:1,name:'于晓璘',sex:'男',country:'中国'},
    {id:2,name:'文太裕',sex:'男',country:'韩国'},
    {id:3,name:'刘演锡',sex:'男',country:'韩国'},
    {id:4,name:'杨颖',sex:'女',country:'中国'}
  ],
  currentPage:1,
  pageSize:4,
  dialogVisible:false,
  row:null,
  employeeList:[]

})

console.log('获取到传递过来的id='+data.id+data.name)


request.get('/employee/selectAll').then(res=>{
  console.log(res)
  data.employeeList= res.data
})

const del=(id) => {
alert("删除id="+id+"的数据")
}

const log=(row)=>{
  data.row = row
  data.dialogVisible=true
}
</script>