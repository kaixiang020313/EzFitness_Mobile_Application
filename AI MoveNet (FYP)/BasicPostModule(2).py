import os
import cv2
import mediapipe as mp

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
        
        # Previous right hand position
        self.prev_right_hand_pos = None
        
        # Counter for right hand movement
        self.right_hand_counter = 0
        
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
                # finding height, width of the image printed
                h, w, c = img.shape
                # Determining the pixels of the landmarks
                cx, cy = int(lm.x * w), int(lm.y * h)
                lmList.append([id, cx, cy])
                if draw:
                    cv2.circle(img, (cx, cy), 5, (255, 0, 0), cv2.FILLED)
        
        return lmList
    
    def countRightHandMovement(self):
        # Get right hand landmarks
        right_hand_landmarks = self.findPosition(self.results)
        
        if right_hand_landmarks:
            # Get the coordinates of the right hand
            rx, ry = right_hand_landmarks[17][1], right_hand_landmarks[17][2]
            
            # If previous right hand position is available
            if self.prev_right_hand_pos:
                prev_rx, prev_ry = self.prev_right_hand_pos
                
                # Check if right hand moved to the right
                if rx > prev_rx:
                    self.right_hand_counter += 1
            
            # Update previous right hand position
            self.prev_right_hand_pos = (rx, ry)

def main():
    detector = PoseDetector()
    
    # Get the path to the video file in the src folder
    current_dir = os.path.dirname(os.path.abspath(__file__))
    video_path = os.path.join(current_dir, "src", "video.mp4")
    
    cap = cv2.VideoCapture(video_path)
    while cap.isOpened():
        ret, img = cap.read()
        if ret:    
            img = detector.findPose(img)
            detector.countRightHandMovement()  # Update right hand movement counter
            cv2.putText(img, f"Right Hand Moves: {detector.right_hand_counter}", 
                        (10, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2)
            cv2.imshow('Pose Detection', img)
        if cv2.waitKey(10) & 0xFF == ord('q'):
            break
            
    cap.release()
    cv2.destroyAllWindows()
    
if __name__ == "__main__":
    main()