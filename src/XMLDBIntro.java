import org.exist.xmldb.EXistResource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XQueryService;

import javax.xml.transform.OutputKeys;
import java.io.File;
import java.util.Scanner;

/**
 * Created by 47276138y on 22/03/17.
 */
public class XMLDBIntro {

        private static String URI = "xmldb:exist://localhost:8080/exist/xmlrpc/db";
        private static String driver = "org.exist.xmldb.DatabaseImpl";
        private static String collectionName = "Trantor";
        private static String factbookName = "Factbook.xml";
        private static String mondialName = "mondial.xml";
        private static String USERNAME = "Pau";
        private static String PASSWORD = "12345";


    public static void main(String args[]) throws XMLDBException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Scanner in = new Scanner(System.in);
        char finalOpt;

        do {

            //Menú per seleccionar alguna de les opcions.
            System.out.println("\t\t***********eXistDB***********\n");
            System.out.println("\n");
            System.out.println("\t\tSelecciona una opció:\n");
            System.out.println("\t\tAfegeix una col·lecció (1)");
            System.out.println("\t\tAfegeix un Fitxer de Recursos (2)");
            System.out.println("\t\tRetornar un Recurs (3)");
            System.out.println("\t\tConsulta sobre una col·lecció (4)");


            int opt = in.nextInt();

            switch (opt) {
                case 1:
                    //Permeto crear una Col·lecció de Recursos.
                    addCollection();
                    break;

                case 2:
                    //Afegeixo un Recurs a una de les col·leccions creades.
                    addResourceFile(factbookName);
                    break;
                case 3:
                    //Retorno un Recurs passant la Col·lecció i el nom del Recurs.
                    returnResource(collectionName, mondialName);
                    break;

                case 4:
                    //Consulta a tots els Recursos de la col·lecció amb un sola query.
                    queryInAllResources(collectionName);
                    break;

                case 5:
                    //Faig una consulta a un sol Recurs de la col·lecció a partir d'una query.
                    queryInOneResource(collectionName, factbookName);
                    break;
            }

            System.out.println("\t\tVols continuar operant a eXist? (s/n)");
            finalOpt = in.next().charAt(0);
            if(finalOpt == 'n'){
                System.out.println("Adéu!");
            }

        }while(finalOpt=='s');
    }


    /**
     * Mètode que fa una query a un dels recursos d'una Col·lecció.
     * @param collection
     * @param resourceFile
     */
    private static void queryInOneResource(String collection, String resourceFile) {

        Collection col = null;
        Resource res = null;
        String xquery = "//country";

        try {
            col = DatabaseManager.getCollection(URI + "/" + collection);
            XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
            xqs.setProperty("indent", "yes");

            CompiledExpression compiled = xqs.compile("doc('"+ resourceFile + "')" + xquery);
            ResourceSet result = xqs.execute(compiled);
            ResourceIterator i = result.getIterator();

            while (i.hasMoreResources()) {
                res = i.nextResource();
                System.out.println(res.getContent());
            }

        } catch (XMLDBException e) {
            e.printStackTrace();
        }finally {
            //Tot seguit, netejo els recursos per tornar a operar
            try { ((EXistResource)res).freeResources(); } catch(XMLDBException xe) {xe.printStackTrace();}
        }
    }


    /**
     * Mètode que fa una query a tots els recursos de la Col·lecció
     * @param colName
     */
    private static void queryInAllResources(String colName) {

        Collection col = null;
        Resource res = null;
        String xquery = "//country";

        try {
            col = DatabaseManager.getCollection(URI + "/" + colName);
            XQueryService xqs = (XQueryService) col.getService("XQueryService", "1.0");
            xqs.setProperty("indent", "yes");

            CompiledExpression compiled = xqs.compile(xquery);
            ResourceSet result = xqs.execute(compiled);
            ResourceIterator i = result.getIterator();

            while (i.hasMoreResources()) {

                res = i.nextResource();
                System.out.println(res.getContent());
            }

        } catch (XMLDBException e) {
            e.printStackTrace();
        }finally {
            //Tot seguit, netejo els recursos per tornar a operar
            try { ((EXistResource)res).freeResources(); } catch(XMLDBException xe) {xe.printStackTrace();}
        }
    }




    /**
     * Mètode que retorna un Recurs dins d'una Col·lecció.
     * @param collection
     * @param resName
     */
    private static void returnResource(String collection, String resName) {

        //Primer faig una crida a la col·lecció per tractar-la.
        Collection col = null;
        XMLResource res = null;

        try {
            col = DatabaseManager.getCollection(URI + "/" + collection);
            col.setProperty(OutputKeys.INDENT, "no");
            res = (XMLResource) col.getResource(resName);

            if (res == null) {
                System.out.println("Document not found!");
            } else {
                System.out.println(res.getContent());
            }

            //Si ha rebut el recurs, després de retornar-lo faig el clean-up.
            if (res != null) {
                ((EXistResource) res).freeResources();
            }

        } catch (XMLDBException e) {
            e.printStackTrace();
        }
    }


    /**
     * Establim de forma estàtica la connexió a la base de dades.
     */
    static {

        Class cl = null;
        try {
            cl = Class.forName(driver);

            //Instancio la Base de dades.
            Database database = (Database) cl.newInstance();
            database.setProperty("create-database", "true");

            //Creo un Manager per poder fer modificacions a la Base de dades.
            DatabaseManager.registerDatabase(database);

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | XMLDBException e) {
            e.printStackTrace();
        }
    }




    /**
     * Mètode que afegeix una Col·lecció a eXists DB
     * @throws XMLDBException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
        static void addCollection(){

            Scanner in = new Scanner(System.in);

            //Adquireixo el Parent on crearé la nova Col·lecció.
            Collection parent = null;
            try {
                parent = DatabaseManager.getCollection(URI, USERNAME, PASSWORD);
                CollectionManagementService cms = (CollectionManagementService) parent.getService("CollectionManagementService", "1.0");

                //Pregunto a l'usuari quin nom vol per a la col·lecció.
                System.out.println("Entra el nom de la nova col·lecció:");
                String colName = in.nextLine();
                cms.createCollection(colName);

                parent.close();

            } catch (XMLDBException e) {
                e.printStackTrace();
            }

        }




    /**
     * Mètode que crea un Fitxer de Recursos dins d'una col·lecció.
     * @param fileName
     */
    private static void addResourceFile(String fileName){

            //Afegir el recurs que farem servir.
            File f = new File(fileName);

            String col = "Trantor";

            Collection parent = null;
            try {
                parent = DatabaseManager.getCollection(URI + "/" + col, USERNAME,PASSWORD);
                XMLResource res = (XMLResource) parent.createResource(fileName,"XMLResource");

                //Entro el contingut del fitxer.
                res.setContent(f);
                parent.storeResource(res);
                System.out.print("storing document...");
                parent.close();
                System.out.println("ok.");

            } catch (XMLDBException e) {
                e.printStackTrace();
            }
        }
    }


