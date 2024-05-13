import os
import cv2
import mediapipe as mp
from flask import Flask, request, jsonify

app = Flask(__name__)

class PoseDetector():
    def __init__(self, mode=False, complexity=1, smooth_landmarks=True,
                 enable_segmentation=False, smooth_segmentation=True,
                 detectionCon=0.5, trackCon=0.5):
        self.mode = mode 
        self.complexity = complexity
        self.smooth_landmarks = smooth_landmarks
        self.enable_segmentation = enable_segmentation
        self.smooth_segmentation = smooth_segmentation
        self.detectionCon = detectionCon
        self.trackCon = trackCon
        
        self.mpDraw = mp.solutions.drawing_utils
        self.mpPose = mp.solutions.pose
        self.pose = self.mpPose.Pose(self.mode, self.complexity, self.smooth_landmarks,
                                     self.enable_segmentation, self.smooth_segmentation,
                                     self.detectionCon, self.trackCon)  

    def findPose(self, img, draw=True):
        imgRGB = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        self.results = self.pose.process(imgRGB)
        
        if self.results.pose_landmarks:
            if draw:
                self.mpDraw.draw_landmarks(img, self.results.pose_landmarks,
                                           self.mpPose.POSE_CONNECTIONS)
                
        return img
    
    def findPosition(self, img, draw=True):
        lmList = []
        if self.results.pose_landmarks:
            for id, lm in enumerate(self.results.pose_landmarks.landmark):
                h, w, c = img.shape
                cx, cy = int(lm.x * w), int(lm.y * h)
                lmList.append([id, cx, cy])
                
                # Draw circles around shoulder and elbow joints (landmarks 11, 12, 13, 14)
                if id in [11, 12, 13, 14]:
                    cv2.circle(img, (cx, cy), 5, (255,0,0), cv2.FILLED)
                
        return lmList


def isAbs(curr_body_points):
    left_hip, right_hip = curr_body_points[23], curr_body_points[24]
    left_shoulder, right_shoulder = curr_body_points[11], curr_body_points[12]
    left_knee, right_knee = curr_body_points[25], curr_body_points[26]
    
    # 如果膝盖高于臀部和肩膀，则认为是 "abs" 动作
    return left_knee[1] > left_hip[1] and right_knee[1] > right_hip[1] \
        and left_knee[1] > left_shoulder[1] and right_knee[1] > right_shoulder[1]


def isPushUp(prev_body_points, curr_body_points, threshold=3):
    prev_left_shoulder, prev_right_shoulder = prev_body_points[11], prev_body_points[12]
    curr_left_shoulder, curr_right_shoulder = curr_body_points[11], curr_body_points[12]
    
    # 计算当前帧和上一帧肩膀位置的垂直差异
    left_shoulder_diff = curr_left_shoulder[1] - prev_left_shoulder[1]
    right_shoulder_diff = curr_right_shoulder[1] - prev_right_shoulder[1]
    
    # 如果差异小于阈值，则忽略变化
    if abs(left_shoulder_diff) < threshold and abs(right_shoulder_diff) < threshold:
        return False
    
    # 如果当前帧的肩膀比上一帧的肩膀高，则返回True
    return left_shoulder_diff > 0 or right_shoulder_diff > 0


def isBoxing(curr_body_points):
    # 定义要检查的关键点索引
    right_hand_keypoints = [12, 14, 16, 18, 20, 22]
    left_hand_keypoints = [11, 13, 15, 17, 19, 21]
    
    # 检查右手关键点是否在一条直线上
    if isStraightLine(curr_body_points, right_hand_keypoints):
        return True
    
    # 检查左手关键点是否在一条直线上
    if isStraightLine(curr_body_points, left_hand_keypoints):
        return True
    
    return False


def isHIIT(curr_body_points):
    # 定义要检查的关键点索引
    right_hand_keypoints = [16, 18, 20, 22]
    left_hand_keypoints = [11, 15, 17, 19, 21]
    
    # 获取右手和左手关键点的y坐标
    right_hand_y = [curr_body_points[i][2] for i in right_hand_keypoints]
    left_hand_y = [curr_body_points[i][2] for i in left_hand_keypoints]
    
    # 获取肩膀的y坐标
    right_shoulder_y = curr_body_points[12][2]
    left_shoulder_y = curr_body_points[11][2]
    
    # 如果右手的最小y坐标大于左肩膀的y坐标，或者左手的最小y坐标大于右肩膀的y坐标，则认为是HIIT
    if min(right_hand_y) > left_shoulder_y or min(left_hand_y) > right_shoulder_y:
        return True
    
    return False

def isLeg(curr_body_points):
    left_hip, right_hip = curr_body_points[23], curr_body_points[24]
    left_knee, right_knee = curr_body_points[25], curr_body_points[26]
    
    # 如果臀部低于膝盖，则认为是 "Leg" 动作
    return left_hip[1] < left_knee[1] and right_hip[1] < right_knee[1]


def isYoga(curr_body_points):
    # 定义要检查的关键点索引
    yoga_keypoints = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 16, 17, 18, 19, 20, 21, 22]
    
    # 获取手腕关键点和头部/眼睛关键点的y坐标
    wrist_y = [curr_body_points[i][2] for i in [15, 16, 17, 18, 19, 20, 21, 22]]
    head_eye_y = [curr_body_points[i][2] for i in yoga_keypoints]
    
    # 如果手腕高于头部和眼睛的关键点，则认为是 "Yoga" 动作
    return min(wrist_y) < min(head_eye_y)

def isTabata(curr_body_points):
    # 定义要检查的关键点索引
    left_hip, right_hip = curr_body_points[23], curr_body_points[24]
    left_knee, right_knee = curr_body_points[25], curr_body_points[26]
    left_ankle, right_ankle = curr_body_points[27], curr_body_points[28]
    left_heel, right_heel = curr_body_points[29], curr_body_points[30]
    
    # 检查右腿是否伸直，左腿是否弯曲
    if left_hip[1] < left_knee[1] and left_knee[1] < left_ankle[1] and left_ankle[1] < left_heel[1] \
        and right_hip[1] < right_knee[1] and right_knee[1] < right_ankle[1] and right_ankle[1] < right_heel[1]:
        return True
    
    # 检查左腿是否伸直，右腿是否弯曲
    if left_knee[1] < left_hip[1] and left_ankle[1] < left_knee[1] and left_heel[1] < left_ankle[1] \
        and right_knee[1] < right_hip[1] and right_ankle[1] < right_knee[1] and right_heel[1] < right_ankle[1]:
        return True
    
    return False

def isStraightLine(points, keypoint_indices):
    # 获取关键点的坐标
    x_coords = [points[i][1] for i in keypoint_indices]
    y_coords = [points[i][2] for i in keypoint_indices]
    
    # 利用线性回归检查是否在一条直线上
    x_mean = sum(x_coords) / len(x_coords)
    y_mean = sum(y_coords) / len(y_coords)
    
    numerator = sum((x - x_mean) * (y - y_mean) for x, y in zip(x_coords, y_coords))
    denominator = sum((x - x_mean) ** 2 for x in x_coords)
    
    if denominator == 0:
        return False
    
    slope = numerator / denominator
    
    # 如果斜率接近于零，则认为在一条直线上
    return abs(slope) < 0.1

@app.route('/detect_movement', methods=['POST'])
def detect_movement():
    # 获取 POST 请求中的视频 URL
    #video_url = request.form.get('video_url')
    video_url = "src/video.mp4"

    # 初始化姿势检测器
    detector = PoseDetector()
    
    # 从视频 URL 创建视频捕获对象
    cap = cv2.VideoCapture(video_url)

    # 获取视频帧率和每帧之间的间隔时间
    fps = cap.get(cv2.CAP_PROP_FPS)
    interval = int(1000 / fps)

    # 保存上一帧的关键点位置
    prev_body_points = None

    # 存储每个动作的结果
    results = {
        "Abs": False,
        "PushUp": False,
        "Boxing": False,
        "HIIT": False,
        "Leg": False,
        "Yoga": False,
        "Tabata": False
    }

    # 逐帧处理视频
    while cap.isOpened():
        ret, img = cap.read()
        if ret:
            # 查找并绘制姿势关键点
            img = detector.findPose(img)
            body_points = detector.findPosition(img)
            
            # 如果上一帧的关键点位置已存在，则与当前帧进行比较
            if prev_body_points is not None:
                if(isPushUp):
                    results["PushUp"] = True
                if(isAbs):
                    results["Abs"] = True
            
            # 保存当前帧的关键点位置以供下一帧使用
            prev_body_points = body_points
            
            # 显示带有动作标签的图像
            cv2.imshow('Pose Detection', img)
        
        # 检查是否按下 'q' 键以退出循环
        if cv2.waitKey(interval) & 0xFF == ord('q'):
            break
            
    # 释放视频捕获对象并关闭所有窗口
    cap.release()
    cv2.destroyAllWindows()

    # 返回每个动作的检测结果
    return jsonify(results)

if __name__ == "__main__":
    app.run(host='192.168.1.82', port=5000)
