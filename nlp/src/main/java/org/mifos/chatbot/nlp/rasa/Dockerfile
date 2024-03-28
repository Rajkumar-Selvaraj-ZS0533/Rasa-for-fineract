FROM python:3.9
RUN pip install rasa
RUN pip install python-socketio
RUN pip install python-engineio
RUN pip install Flask-SocketIO
# Use subdirectory as working directory
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Install any needed dependencies specified in requirements.txt
#RUN #pip install rasa
RUN rasa train

# Expose the Rasa server port
EXPOSE 5005

# Start the Rasa server
#CMD ["rasa", "run", "-m", "models", "--enable-api", "--cors", "*", "--debug"]
CMD ["sh", "/app/script.sh"]