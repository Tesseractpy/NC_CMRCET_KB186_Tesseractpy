# Open the file in read mode 
text = open("out0.txt", "r")
file1 = open("myfile270up.txt","w+")
file2 = open("myfile10up.txt","w+")
file3 = open("myfile10dwn.txt","w+")

# Create an empty dictionary 
d = dict() 

# Loop through each line of the file 
for line in text: 
	# Remove the leading spaces and newline character 
	line = line.strip() 

	# Convert the characters in line to 
	# lowercase to avoid case mismatch 
	line = line.lower() 

	# Split the line into words 
	words = line.split(" ") 

	# Iterate over each word in line 
	for word in words: 
		# Check if the word is already in dictionary 
		if word in d: 
			# Increment count of word by 1 
			d[word] = d[word] + 1
		else: 
			# Add the word to dictionary with count 1 
			d[word] = 1

# Print the contents of dictionary 
for key in list(d.keys()): 
	wrt = str(key)+"\n"
	if(d[key]>300 and (key!="")):
		file1.write(wrt)
	elif(d[key]<301 and (d[key]>10) and (key!="")):
		file2.write(wrt)
	elif(d[key]<11 and (key!="")):
		file3.write(wrt)
	print(key, ":", d[key]) 
