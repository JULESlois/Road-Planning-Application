# 音乐文件目录

## 说明
此目录用于存放音乐播放器的音频文件。

## 支持的文件格式
- MP3 (.mp3)
- WAV (.wav)
- OGG (.ogg)
- M4A (.m4a)

## 推荐文件结构
```
music/
├── nature.mp3          # 轻音乐 - 自然之声
├── moonlight.mp3       # 古典音乐 - 月光奏鸣曲
├── jazz.mp3           # 爵士乐 - 夜曲
├── electronic.mp3     # 电子音乐 - 未来之声
└── folk.mp3          # 民谣 - 山间小路
```

## 添加音乐文件步骤
1. 将音频文件复制到此目录
2. 确保文件名与 `MusicPlayer.vue` 中的 `playlist` 配置一致
3. 更新 `MusicPlayer.vue` 中的 `duration` 字段为实际音频时长（秒）

## 注意事项
- 文件大小建议控制在 10MB 以内
- 音频质量建议使用 128kbps 或 192kbps
- 确保音频文件没有版权问题
- 支持的音乐文件会自动显示在播放列表中

## 示例音乐文件获取
您可以从以下来源获取免费音乐：
- Free Music Archive
- ccMixter
- Incompetech (Kevin MacLeod)
- Bensound
- 或其他免费音乐资源网站 