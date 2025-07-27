@echo off
echo 正在清理项目...
flutter clean

echo 正在获取依赖项...
flutter pub get

echo 正在运行代码生成...
flutter packages pub run build_runner build --delete-conflicting-outputs

echo 项目清理完成！
pause
