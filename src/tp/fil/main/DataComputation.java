package tp.fil.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.modisco.java.emf.JavaPackage;

import com.google.common.collect.Lists;

import org.eclipse.modisco.java.Model;
import org.eclipse.modisco.java.PrimitiveType;
//import org.eclipse.modisco.java.Package;
//import org.eclipse.modisco.java.AbstractTypeDeclaration;
//import org.eclipse.modisco.java.BodyDeclaration;
//import org.eclipse.modisco.java.ClassDeclaration;
//import org.eclipse.modisco.java.FieldDeclaration;

public class DataComputation {

	private static Resource dataMetamodel;
	private static EPackage dataPackage;
	private static Resource javaModel;

	public static void main(String[] args) {
		try {
			Resource dataModel;

			// Create and configure resource set
			ResourceSet resSet = new ResourceSetImpl();
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

			// Load Java & Data metamodel
			JavaPackage.eINSTANCE.eClass();

			dataMetamodel = resSet.createResource(URI.createFileURI("src/tp/fil/resources/Data.ecore"));
			dataMetamodel.load(null);
			EPackage.Registry.INSTANCE.put("http://data", dataMetamodel.getContents().get(0));

			// Load Java model
			javaModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_java.xmi"));
			javaModel.load(null);

			// Initiate Data model with a "Model" root object/element
			dataModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_data.xmi"));

			dataPackage = (EPackage) dataMetamodel.getContents().get(0);
			EClass modelClass = (EClass) dataPackage.getEClassifier("Model");
			EObject modelObject = dataPackage.getEFactoryInstance().create(modelClass);

			Model javaModelRootElement = (Model) javaModel.getContents().get(0);
			modelObject.eSet(modelClass.getEStructuralFeature("name"), javaModelRootElement.getName());

			dataModel.getContents().add(modelObject);

			// Build the actual Data model by navigating the Java model
			// and creating the appropriate Data model elements...
			TreeIterator<EObject> iterator = javaModel.getAllContents();

			/*
			 * Beginning of the part to be completed...
			 */

			while (iterator.hasNext()) {
				EObject javaModelObject = iterator.next();

				if (javaModelObject.eClass().getName().equals("Package")) {
					EClass packageClass = javaModelObject.eClass();
					String packageName = (String) javaModelObject.eGet(packageClass.getEStructuralFeature("name"));
					boolean packageIsProxy = (boolean) javaModelObject
							.eGet(packageClass.getEStructuralFeature("proxy"));

//					EObject parentPackage = (EObject)javaModelObject.eR(javaModelObject.eClass().getEStructuralFeature("package"));
//					System.out.println(parentPackage.eClass());
//					String parentPackageName = (String) parentPackage.eGet(javaModelObject.eClass().getEStructuralFeature("name"));

					if (packageName.equals("model") && !packageIsProxy) {
						List<EObject> javaModelClassDeclarations = javaModelObject.eContents().stream()
								.filter(obj -> obj.eClass().getName().equals("ClassDeclaration"))
								.collect(Collectors.toList());

						List<EObject> classEObjects = javaModelClassDeclarations.stream()
								.map(dec -> getClassEObjectFromClassDeclaration(dec)).collect(Collectors.toList());

						modelObject.eSet(modelClass.getEStructuralFeature("classes"), classEObjects);

					}
				}
			}

			/*
			 * End of the part to be completed...
			 */

			// Serialize Data model
			dataModel.save(null);

			// Unload models
			javaModel.unload();
			dataModel.unload();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static EObject getClassEObjectFromClassDeclaration(EObject classDeclaration) {

		Map<String, Object> classObjectProperties = new HashMap<>();

		classObjectProperties.put("name", DataComputation.<String>getEObjectProperty(classDeclaration, "name"));

		List<EObject> classDeclarationBodyDeclarations = DataComputation
				.<List<EObject>>getEObjectProperty(classDeclaration, "bodyDeclarations");

		List<EObject> fieldDeclarations = classDeclarationBodyDeclarations.stream()
				.filter(dec -> dec.eClass().getName().equals("FieldDeclaration")).collect(Collectors.toList());

		List<EObject> attributes = new ArrayList<EObject>();
		List<EObject> relations = new ArrayList<EObject>();
		
		for(EObject fieldDeclaration : fieldDeclarations ) {
			EObject attType = DataComputation 
					.<EObject>getEObjectProperty(DataComputation.<EObject>getEObjectProperty(fieldDeclaration, "type"), "type");
	
			String attTypeName = DataComputation.<String>getEObjectProperty(attType, "name");

			boolean isClassDeclaration = attType.eClass().getName().equals("ClassDeclaration");
			
			boolean isClassDeclarationFromModelPackage = isClassDeclaration && DataComputation.<String>getEObjectProperty(DataComputation.<EObject>getEObjectProperty(attType, "package"), "name").equals("model");
			
			EClass primitiveTypeClass = (EClass) JavaPackage.eINSTANCE.getEClassifier("PrimitiveType");

			
			if(isClassDeclarationFromModelPackage) {
				EObject newRelation = createModelClassInstance("Relation", Map.of("to", attTypeName, "multiple", false));
				relations.add(newRelation);
			} else if (primitiveTypeClass.isSuperTypeOf(attType.eClass()) || attTypeName.split("\\.").length == 1){				
				String attName = DataComputation.<String>getEObjectProperty(
						DataComputation.<List<EObject>>getEObjectProperty(fieldDeclaration, "fragments").get(0), "name");

				EObject newAttribute = createModelClassInstance("Attribute", Map.of("name", attName, "type", attTypeName));
				attributes.add(newAttribute);
			}
				
				
		}
		
		System.out.println(relations);
		
		classObjectProperties.put("relations", relations);
	
		classObjectProperties.put("attributes", attributes);
		

		EObject classObject = createModelClassInstance("Class", classObjectProperties);

		return classObject;

	}

	private static <T> T getEObjectProperty(EObject object, String propertyName) {
		EClass objectClass = object.eClass();
		return (T) object.eGet(objectClass.getEStructuralFeature(propertyName));
	}

	private static EClass getModelClass(String className) {
		return (EClass) dataPackage.getEClassifier(className);
	}

	private static EObject createModelClassInstance(String className, Map<String, Object> properties) {
		EClass modelClass = getModelClass(className);
		EObject instance = (EObject) dataPackage.getEFactoryInstance().create(modelClass);
		if (properties != null) {
			for (Entry<String, Object> property : properties.entrySet()) {
				instance.eSet(modelClass.getEStructuralFeature(property.getKey()), property.getValue());
			}
		}
		return instance;
	}

}
