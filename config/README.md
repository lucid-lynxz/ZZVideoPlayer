抽取通用的签名以及依赖关系配置

# 使用方法
1. 将 `config` 目录复制到项目根目录下, 与 `app/` 模块同级;
2. 复制实际的签名文件替换 `config/sb.jks`, 并同步修改 `config/sign.gralde` 中的签名信息;
3. 在 `project/build.gradle` 中修改仓库信息:
```gradle
allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
}
```
4. 在 `app/build.gradle` 中添加依赖:
```gradle
// app/build.gradle
apply from: "../config/dependencies.gradle"
apply from: "../config/sign.gradle

android{
    buildTypes {
        debug {
            signingConfig signingConfigs.release
        }
        release {
            signingConfig signingConfigs.release}
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation(libs.appcompat)
    implementation(libs.constraintLayout)

    // 依赖就可以改成如此形式了, 在多模块开发时会很方便
    // rx
    compile(libs.rxJava2)
    compile(libs.rxAndroid2)
    compile(libs.rxPermission2)
}
```
