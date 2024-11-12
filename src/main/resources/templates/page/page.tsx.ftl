import CRUD from "@/components/Gyrfalcon/CRUD";
import { delete${entityClassName}, delete${entityClassName}s, ${entityCode}Page, save${entityClassName} } from "@/services/zhiwei/${entityCode}";
import { ActionType, ProColumns,<#list components as c> ${c},</#list> } from "@ant-design/pro-components";
import { useRef } from "react";

const ${entityClassName}: React.FC = () => {

// 表格列
const columns: ProColumns<API.${entityClassName}VO>[] = [
<#list properties as p>
    <#if p.isList>
        {
            title: '${p.propertyName}',
            dataIndex: '${p.propertyCode}',
            search: ${p.isSearch?string('true', 'false')},
        <#if p.isArray>
            render: (_, record) => {
                if(record.m){
                    const array = [...record.m];
                    return array.join(', ');
                }
                return '-';
            },
        </#if>
        },
    </#if>
</#list>
    ];

    const ${entityCode}Form = <>
<#list properties as p>
    <#if p.isForm>
        <${p.componentName}<#if p.componentName == "ProFormRadio" || p.componentName == "ProFormCheckbox">.Group</#if>
            rules={[
                {
                    required: ${p.isRequired?string('true', 'false')},
                    message: "请输入${p.propertyName}",
                }
            ]}
            name="${p.propertyCode}"
            label="${p.propertyName}"
            placeholder="请输入${p.propertyName}"
        <#if p.componentName == "ProFormRadio" || p.componentName == "ProFormCheckbox" || p.componentName == "ProFormSelect">
            options={[
              {
                label: 'item 1',
                value: 'a',
              },
              {
                label: 'item 2',
                value: 'b',
              },
              {
                label: 'item 3',
                value: 'c',
              },
            ]}
        </#if>
        <#if p.componentName == "ProFormTreeSelect">
            fieldProps={{
                treeData: [
                    {
                        value: 'parent 1',
                        title: 'parent 1',
                        children: [
                            {
                                value: 'parent 1-0',
                                title: 'parent 1-0',
                                children: [
                                    {
                                        value: 'leaf1',
                                        title: 'leaf1',
                                    },
                                    {
                                        value: 'leaf2',
                                        title: 'leaf2',
                                    },
                                    {
                                        value: 'leaf3',
                                        title: 'leaf3',
                                    },
                                ],
                            },
                            {
                                value: 'parent 1-1',
                                title: 'parent 1-1',
                                children: [
                                    {
                                        value: 'leaf11',
                                        title: 'leaf11',
                                    },
                                ],
                            },
                        ],
                    },
                ]
            }}
        </#if>
        />
    </#if>
</#list>
    </>;

    const tableRef = useRef<ActionType>();

    return (
        <>
            <CRUD
                tableRef={tableRef}
                title="${entityName}"
                columns={columns}

                formWidth={400}
                createForm={${entityCode}Form}
                updateForm={${entityCode}Form}

                handlePage={${entityCode}Page}
                handleCreate={save${entityClassName}}
                handleUpdate={save${entityClassName}}
                handleDelete={delete${entityClassName}}
                handleBatchDelete={delete${entityClassName}s}
            />
        </>
    );
};

export default ${entityClassName};