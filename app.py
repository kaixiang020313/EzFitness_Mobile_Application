import pytesseract
from PIL import Image
import os
from flask import Flask, request, jsonify

# Path to Tesseract executable
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# Initialize Flask app
app = Flask(__name__)

# Function to extract words from image using OCR
def extract_words(image_path, x1, y1, x2, y2):
    try:
        # Open the image file
        with Image.open(image_path) as img:
            # Crop the image to the specified region
            region = img.crop((x1, y1, x2, y2))

            # Perform OCR on the cropped region
            data = pytesseract.image_to_data(region, output_type=pytesseract.Output.DICT, config='--psm 6')

            # Organize words into rows based on their positions
            rows = {}
            for i in range(len(data['text'])):
                if int(data['conf'][i]) > -1:
                    (x, y) = (data['left'][i], data['top'][i])
                    row = rows.get(y, [])
                    row.append(data['text'][i])
                    rows[y] = row
            
            # Sort rows by Y-coordinate to maintain their order
            sorted_rows = sorted(rows.items(), key=lambda item: item[0])
            
            # Extract words from recognized text
            extracted_text = '\n'.join([' '.join(row) for _, row in sorted_rows])
            
            return extracted_text
    except Exception as e:
        print("Error occurred during OCR:", e)
        return None

# Function to process uploaded image
def process_image(image_file):
    try:
        # Define coordinates for each region to extract
        regions = [
            (127, 144, 203, 164),
            (127, 168, 205, 187),
            (205, 144, 280, 187),
            (136, 192, 188, 210),
            (282, 145, 358, 210),
            (139, 215, 184, 233),
            (361, 169, 431, 200),
            (199, 308, 419, 322),
            (240, 397, 345, 412),
            (296, 424, 379, 439),
            (393, 475, 436, 504),
            (393, 509, 434, 534), 
            (393, 542, 434, 565),
            (393, 572, 435, 593),
            (393, 603, 436, 625),
            # Add more regions as needed
        ]

        extracted_info = {}  # Dictionary to store extracted information
        
        # Extract information from each region
        for i, (x1, y1, x2, y2) in enumerate(regions, start=1):
            extracted_text = extract_words(image_file, x1, y1, x2, y2)
            if extracted_text:
                extracted_info[f"Region {i}"] = extracted_text

        return extracted_info
    except Exception as e:
        print("Error occurred during image processing:", e)
        return None

# Route to handle image upload and processing
@app.route('/process_image', methods=['POST'])
def upload_image():
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'No image uploaded'})

        image_file = request.files['image']
        if image_file.filename == '':
            return jsonify({'error': 'No selected image'})

        # Save the uploaded image temporarily
        temp_image_path = 'temp_image.png'
        image_file.save(temp_image_path)

        # Process the uploaded image
        extracted_info = process_image(temp_image_path)

        # Remove the temporary image file
        os.remove(temp_image_path)

        if extracted_info:
            return jsonify(extracted_info)  # Return extracted information
        else:
            return jsonify({'error': 'Failed to process image'})

    except Exception as e:
        print("Error occurred during image upload and processing:", e)
        return jsonify({'error': 'Internal server error'})

# Run the Flask app
if __name__ == '__main__':
    app.run(host='192.168.1.82', port=5000)
