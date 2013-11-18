class ParcelgenGenerator extends DefaultTask {

	@Input
	def parcelgenLoc = project.file(project.projectDir.getAbsolutePath() +
		"/scripts/parcelgen.py")
	
	@Input
	def sourceDir = project.file(project.projectDir.getAbsolutePath() +
		"/src/main/java")
	
	@Input
	def parcelsDir = project.file(project.projectDir.getAbsolutePath() +
		"/parcels")

	@Input
	def parcelSourcesDir

	File[] getJsonFiles() {
		allFiles = parcelsDir.listFiles()
		def jsonFiles = []
		count = 0

		// If a file ends in .json we assume it is a parcel
		allFiles.each { file ->
			if (file.toString().endsWith(".json")) {
				jsonFiles[count] = file
				count++
			}
		}

		return jsonFiles
	}

	boolean needToRegenerate(parcel) {
		def parcelName = parcel.getName()
		// We just want the class name without any json
		parcelName = parcelName.substring(0, parcelName.lastIndexOf("."))
		parcelSourceFile = new File(parcelSourcesDir, "_" + parcelName + ".java")
		
		// If the parcel hasn't been created yet, we definitely need to
		// regenerate it
		if (!parcelSourceFile.exists()) {
			return true 
		}
	
		// If the parcel source file exists, we only want to create it if the
		// parcel has been modified since the java file was generated
		return parcel.lastModified() > parcelSourceFile.lastModified()
	}

	void generateDirs() {
		parcelsDir.mkdirs()
		sourceDir.mkdirs()
		parcelSourcesDir.mkdirs()
	}

	@TaskAction
	def generate() {
		parcelsDir = project.file(parcelsDir)
		parcelSourcesDir = project.file(parcelSourcesDir)
		generateDirs()
		getJsonFiles().each { parcel ->
			if(needToRegenerate(parcel)) {
				Runtime.getRuntime().exec("python " + parcelgenLoc + " " +
				 	parcel.getAbsolutePath() + " " + sourceDir.getAbsolutePath())
			}
		}
	}
}

task parcelGenerator(type: ParcelgenGenerator) {
	parcelsDir =  "." 
	parcelSourcesDir = project.file(projectDir.getAbsolutePath() +
		"/src/main/java/com/yelp/parcelgen/") 
}

