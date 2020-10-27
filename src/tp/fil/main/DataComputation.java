package tp.fil.main;

import java.io.IOException;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.modisco.java.emf.JavaPackage;
import org.eclipse.modisco.java.Model;
//import org.eclipse.modisco.java.Package;
//import org.eclipse.modisco.java.AbstractTypeDeclaration;
//import org.eclipse.modisco.java.BodyDeclaration;
//import org.eclipse.modisco.java.ClassDeclaration;
//import org.eclipse.modisco.java.FieldDeclaration;


public class DataComputation {
	
	public static void main(String[] args) {
		try {
			Resource javaModel;
			Resource dataModel;
			Resource dataMetamodel;
			
			//Create and configure resource set
			ResourceSet resSet = new ResourceSetImpl();
			resSet.getResourceFactoryRegistry().
				getExtensionToFactoryMap().
				put("ecore", new EcoreResourceFactoryImpl());
			resSet.getResourceFactoryRegistry().
				getExtensionToFactoryMap().
				put("xmi", new XMIResourceFactoryImpl());
			
			//Load Java & Data metamodel
			JavaPackage.eINSTANCE.eClass();
			
			dataMetamodel = resSet.createResource(URI.createFileURI("src/tp/fil/resources/Data.ecore"));
			dataMetamodel.load(null);
			EPackage.Registry.INSTANCE.
				put("http://data", dataMetamodel.getContents().get(0));
			
			//Load Java model
			javaModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_java.xmi"));
			javaModel.load(null);
			
			//Initiate Data model with a "Model" root object/element
			dataModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_data.xmi"));
			
			EPackage dataPackage = (EPackage) dataMetamodel.getContents().get(0);
			EClass modelClass = (EClass) dataPackage.getEClassifier("Model");
			EObject modelObject = dataPackage.getEFactoryInstance().create(modelClass);
			
			Model javaModelRootElement = (Model) javaModel.getContents().get(0);
			modelObject.eSet(modelClass.getEStructuralFeature("name"), javaModelRootElement.getName());
			
			dataModel.getContents().add(modelObject);
			
			//Build the actual Data model by navigating the Java model
			//and creating the appropriate Data model elements...
			TreeIterator<EObject> iterator = javaModel.getAllContents();
			
			/*
			 * Beginning of the part to be completed...
			 */
			
			//TO BE COMPLETED
			
			/*
			 * End of the part to be completed...
			 */
			
			//Serialize Data model
			dataModel.save(null);
			
			//Unload models
			javaModel.unload();
			dataModel.unload();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
