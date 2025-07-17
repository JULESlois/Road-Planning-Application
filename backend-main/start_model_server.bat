@echo off
echo 启动交通流量预测模型服务器...

cd /d "%~dp0src\main\resources"

echo 当前目录: %CD%
echo 检查Python环境...

python --version
if errorlevel 1 (
    echo Python未安装或不在PATH中
    pause
    exit /b 1
)

echo 检查必要的Python包...
python -c "import flask, xgboost, pandas" 2>nul
if errorlevel 1 (
    echo 缺少必要的Python包，正在安装...
    pip install flask xgboost pandas
)

echo 检查模型文件...
if not exist "models\node_volume_model.json" (
    echo 错误: 模型文件不存在: models\node_volume_model.json
    pause
    exit /b 1
)

echo 启动模型服务器...
python model_server.py

pause 