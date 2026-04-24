package com.example.githubuploader.domain

object TemplateGenerator {
    fun generateUnpackYml(): String {
        val dollarSign = "$"
        return """
name: Unpack ZIP

on:
  push:
    paths:
      - '**/*.zip'

jobs:
  unpack:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        
      - name: Find and unpack ZIP files
        run: |
          for zip_file in *.zip; do
            if [ -f "${dollarSign}zip_file" ]; then
              echo "Unpacking ${dollarSign}zip_file..."
              dir_name="${dollarSign}{zip_file%.zip}"
              mkdir -p "${dollarSign}dir_name"
              unzip -o "${dollarSign}zip_file" -d "${dollarSign}dir_name"
              rm "${dollarSign}zip_file"
            fi
          done
          
      - name: Commit and push changes
        run: |
          git config --global user.name "github-actions[bot]"
          git config --global user.email "github-actions[bot]@users.noreply.github.com"
          git add .
          git commit -m "Unpack ZIP files" || echo "No changes to commit"
          git push || true
        """.trimIndent()
    }

    fun generateBuildYml(
        buildBranch: String,
        javaVersion: String,
        gradleVersion: String,
        buildType: String
    ): String {
        val buildTypeCapitalized = buildType.replaceFirstChar { it.uppercase() }
        return """
name: Build Android

on:
  push:
    branches: [ $buildBranch ]
  pull_request:
    branches: [ $buildBranch ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        
      - name: Set up JDK $javaVersion
        uses: actions/setup-java@v4
        with:
          java-version: '$javaVersion'
          distribution: 'temurin'
          cache: 'gradle'
          
      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
        
      - name: Build with Gradle
        run: ./gradlew assemble$buildTypeCapitalized
        
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-$buildType
          path: app/build/outputs/apk/$buildType/*.apk
        """.trimIndent()
    }
}
