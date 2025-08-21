/**
 * 简化的Dokka文档生成配置
 * 为Android漫画阅读应用生成API文档
 */

// 应用Dokka插件到所有子项目
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

// 应用Dokka插件到根项目
apply(plugin = "org.jetbrains.dokka")

// 创建生成所有文档的任务
tasks.register("generateAllDocs") {
    group = "documentation"
    description = "生成所有模块的API文档"
    
    dependsOn("dokkaHtmlMultiModule")
    subprojects.forEach { subproject ->
        dependsOn("${subproject.path}:dokkaHtml")
    }
    
    doLast {
        println("✅ 所有API文档已生成完成")
        println("📖 多模块文档: ${layout.buildDirectory.get()}/dokka/htmlMultiModule/index.html")
        subprojects.forEach { subproject ->
            println("📖 ${subproject.name}模块文档: ${subproject.layout.buildDirectory.get()}/dokka/html/index.html")
        }
    }
}

// 创建清理文档的任务
tasks.register<Delete>("cleanDokka") {
    group = "documentation"
    description = "清理所有生成的Dokka文档"
    
    delete(layout.buildDirectory.dir("dokka"))
    subprojects.forEach { subproject ->
        delete(subproject.layout.buildDirectory.dir("dokka"))
    }
}

// 验证文档完整性的任务
tasks.register("validateDocs") {
    group = "documentation"
    description = "验证生成的文档是否完整"
    
    dependsOn("generateAllDocs")
    
    doLast {
        val multiModuleIndex = layout.buildDirectory.file("dokka/htmlMultiModule/index.html").get().asFile
        if (!multiModuleIndex.exists()) {
            throw GradleException("多模块文档未生成: ${multiModuleIndex.absolutePath}")
        }
        
        subprojects.forEach { subproject ->
            val moduleIndex = subproject.layout.buildDirectory.file("dokka/html/index.html").get().asFile
            if (!moduleIndex.exists()) {
                throw GradleException("${subproject.name}模块文档未生成: ${moduleIndex.absolutePath}")
            }
        }
        
        println("✅ 所有文档验证通过")
    }
}