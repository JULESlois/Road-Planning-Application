from flask import Flask, request, jsonify
import xgboost as xgb
import pandas as pd

app = Flask(__name__)

# === 加载模型 ===
model_path = "models/node_volume_model.json"
model = xgb.XGBRegressor()
model.load_model(model_path)
print("✅ 模型已加载")

@app.route("/predict", methods=["GET", "POST"])
def predict_volume():
    if request.method == "POST":
        data = request.get_json()
        node = data.get("node")
        time = data.get("time")
    else:  # GET 请求
        node = request.args.get("node", type=int)
        time = request.args.get("time", type=int)

    if node is None or time is None:
        return jsonify({"error": "请提供 node 和 time 参数"}), 400

    df = pd.DataFrame([[node, time]], columns=["node", "time"])
    pred = model.predict(df)[0]
    return jsonify({
        "node": node,
        "time": time,
        "volume": float(max(pred, 1))
    })

if __name__ == '__main__':
    app.run(port=5000, debug=True)
