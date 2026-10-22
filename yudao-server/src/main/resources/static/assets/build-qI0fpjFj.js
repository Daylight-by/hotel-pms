import"./rolldown-runtime-aKtaBQYM.js";import{$i as e,Ai as t,Di as n,Gi as r,Ji as i,La as a,Mi as o,Oi as s,Qi as c,Ta as l,Ti as u,aa as d,da as f,ga as p,ji as m,n as h,oa as g,q as _,tt as v,yi as y}from"./form-create-6sasZ53Q.js";import{t as b}from"./useI18n-8MLz5SEV.js";import{c as x}from"./is-CeSbiYHl.js";import{t as S}from"./useMessage-c0QI1EWC.js";import{K as C,d as w}from"./index-CF2YcP3r.js";import{t as T}from"./Dialog-D12F7D9N.js";import{t as E}from"./ContentWrap-DWHFuWDH.js";import{n as D,t as O}from"./java-ALr-_QeE.js";function k(e){let t={className:`attr`,begin:/"(\\.|[^\\"\r\n])*"(?=\s*:)/,relevance:1.01},n={match:/[{}[\],:]/,className:`punctuation`,relevance:0},r=[`true`,`false`,`null`],i={scope:`literal`,beginKeywords:r.join(` `)};return{name:`JSON`,aliases:[`jsonc`],keywords:{literal:r},contains:[t,n,e.QUOTE_STRING_MODE,i,e.C_NUMBER_MODE,e.C_LINE_COMMENT_MODE,e.C_BLOCK_COMMENT_MODE],illegal:`\\S`}}var A={class:`h-[calc(100vh-var(--top-tool-height)-var(--tags-view-height)-var(--app-content-padding)-var(--app-content-padding)-2px)]`},j={key:0,ref:`editor`},M={class:`hljs`},N=o({name:`InfraBuild`,__name:`index`,setup(o){let{t:N}=b(),P=S(),F=p({switchType:[],autoActive:!0,useTemplate:!1,formOptions:{form:{labelWidth:`100px`}},fieldReadonly:!1,hiddenDragMenu:!1,hiddenDragBtn:!1,hiddenMenu:[],hiddenItem:[],hiddenItemConfig:{},disabledItemConfig:{},showSaveBtn:!1,showConfig:!0,showBaseForm:!0,showControl:!0,showPropsForm:!0,showEventForm:!0,showValidateForm:!0,showFormConfig:!0,showInputData:!0,showDevice:!0,appendConfigData:[]}),I=p(),L=p(!1),R=p(``),z=p(-1),B=p(``);C(I);let V=e=>{L.value=!0,R.value=e},H=()=>{V(`生成 JSON`),z.value=0,B.value=I.value.getRule()},U=()=>{V(`生成 Options`),z.value=1,B.value=I.value.getOption()},W=()=>{V(`生成组件`),z.value=2,B.value=G()},G=()=>{let e=I.value.getRule(),t=I.value.getOption();return`<template>
    <form-create
      v-model:api="fApi"
      :rule="rule"
      :option="option"
      @submit="onSubmit"
    ></form-create>
  </template>
  <script setup lang=ts>
    const faps = ref(null)
    const rule = ref('')
    const option = ref('')
    const init = () => {
      rule.value = formCreate.parseJson('${h.toJson(e).replaceAll(`\\`,`\\\\`)}')
      option.value = formCreate.parseJson('${JSON.stringify(t)}')
    }
    const onSubmit = (formData) => {
      //todo 提交表单
    }
    init()
  <\/script>`},K=async e=>{let{copy:t,copied:n,isSupported:r}=w({legacy:!0,source:JSON.stringify(e,null,2)});r?(await t(),l(n)&&P.success(N(`common.copySuccess`))):P.error(N(`common.copyError`))},q=e=>{let t=`json`;return z.value===2&&(t=`xml`),x(e)||(e=JSON.stringify(e,null,2)),D.highlight(e,{language:t,ignoreIllegals:!0}).value||`&nbsp;`};return r(async()=>{D.registerLanguage(`xml`,O),D.registerLanguage(`json`,k)}),(r,o)=>{let p=_,h=c(`fc-designer`),b=E,x=v,S=T,C=e(`dompurify-html`);return i(),s(y,null,[m(b,{"body-style":{padding:`0px`},class:`!mb-0`},{default:d(()=>[u(`div`,A,[m(h,{class:`my-designer`,ref_key:`designer`,ref:I,config:l(F)},{handle:d(()=>[m(p,{size:`small`,type:`primary`,plain:``,onClick:H},{default:d(()=>[...o[2]||=[t(`生成JSON`,-1)]]),_:1}),m(p,{size:`small`,type:`success`,plain:``,onClick:U},{default:d(()=>[...o[3]||=[t(`生成Options`,-1)]]),_:1}),m(p,{size:`small`,type:`danger`,plain:``,onClick:W},{default:d(()=>[...o[4]||=[t(`生成组件`,-1)]]),_:1})]),_:1},8,[`config`])])]),_:1}),m(S,{modelValue:l(L),"onUpdate:modelValue":o[1]||=e=>f(L)?L.value=e:null,title:l(R),"max-height":`600`},{default:d(()=>[l(L)?(i(),s(`div`,j,[m(p,{style:{float:`right`},onClick:o[0]||=e=>K(l(B))},{default:d(()=>[t(a(l(N)(`common.copy`)),1)]),_:1}),m(x,{height:`580`},{default:d(()=>[u(`div`,null,[u(`pre`,null,[g(u(`code`,M,null,512),[[C,q(l(B))]])])])]),_:1})],512)):n(``,!0)]),_:1},8,[`modelValue`,`title`])],64)}}});export{N as default};