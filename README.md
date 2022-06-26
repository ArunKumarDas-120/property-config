# property-config

<details><summary>How To use</summary>
  
  * Add @EnableCustomPropertySource
  * Use to configure Property sources (ClassPath,File and DataBasse)
  
</details>


<details><summary>How to </summary>

   
  <pre>
   property:
      source:
        config:
          -
            from: classpath
            name: xxx
            type: yml
          -
            from: file
            name: yy
            type: properties
          -
            from: DataBase
            query: select * from Table_name
            keyColumn: key
            valueColumn: prop_value
   </pre>
  
</details>