package massim.ankamas.agents;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.eismassim.EnvironmentInterface;
import massim.ankamas.MailService;
import massim.ankamas.TPercept;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.garret.perst.IPersistentSet;
import org.garret.perst.Index;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BasicAgent extends Agent {
    private static final Logger
            logger = LogManager.getLogger(BasicAgent.class);
    private int lastID = -1;
    KieServices KServices;
    KieContainer KContainer;
    KieBase KBase;
    KieSession KSession;
    Storage db;
    Index root;
    IPersistentSet classExtent;

    /**
     * Constructor.
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public BasicAgent(String name, MailService mailbox, EnvironmentInterface ei) {
        super(name, mailbox);

        db = StorageFactory.getInstance().createStorage();
        db.open("./db/" + name + ".dbs", Storage.DEFAULT_PAGE_POOL_SIZE);
        //db.open("x.dbs", Storage.DEFAULT_PAGE_POOL_SIZE);
        root = (Index) db.getRoot(); // get storage root
        if (root == null) {
            // Root is not yet defined: storage is not initialized
            root = db.createIndex(String.class, // key type
                    true); // unique index
            db.setRoot(root);
        }

        classExtent = (IPersistentSet) root.get("TPercept");
        if (classExtent == null) {
            classExtent = db.createSet(); // create class extent
            root.put("TPercept", classExtent);
        }

        KServices = KieServices.Factory.get();
        KContainer = KServices.getKieClasspathContainer();
        KSession = KContainer.newKieSession("KSession" + name);

        KSession.setGlobal("eis", ei);
        KSession.setGlobal("logger", logger);
        KSession.setGlobal("agent", this);

        int previousPerceptID = -1;
        int currentPerceptID = 0;
        // iterator through all instance of the class
        Iterator i = classExtent.iterator();
        logger.info("##### Recovery Start From Perst Database #####");
        while (i.hasNext()) {
            TPercept tpercept = (TPercept) i.next();
            currentPerceptID = tpercept.prcptid;
            if (previousPerceptID != currentPerceptID) {
                logger.info("##### " + this.getName() + " #####");
                previousPerceptID = currentPerceptID;
            }
            KSession.insert(tpercept);
            logger.info(tpercept);
        }
        logger.info("##### Recovery End From Perst Database #####");
    }

    @Override
    public void handlePercept(Percept percept) {
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }

    @Override
    public Action step(int stepCount) {
        List<Percept> percepts = getPercepts();
        logger.info("**********  " + this.getName() + "  **********");


        for (Percept percept : percepts) {
            TPercept tprcpt = new TPercept();
            tprcpt.prcpt = percept;
            tprcpt.tmstmp = (new Date()).getTime();
            tprcpt.prcptid = stepCount;

            //if(tprcpt.prcpt.getName().equals("lastAction"))
                //System.out.println(tprcpt.prcpt.getParameters().get(0).toString());
            /*System.out.println("aaaaaaaa" + percept.getName() + " " + percept.getParameters().toString());
            System.out.println("bbbbbbbb" + percept.getName() + " " + percept.getParameters().isEmpty());
            System.out.println("cccccccc" + percept.getName() + " " + percept.getParameters().size());
            if (percept.getName().equals("lastActionResult"))
                System.out.println("dddddddd" + percept.getName() + " " + percept.getParameters());*/

            classExtent.add(tprcpt);
            logger.info(tprcpt);
            KSession.insert(tprcpt);

            /* Default code which comes from massim javaagents project
            if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);
                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        lastID = id;
                        return new Action("move", new Identifier("n"));
                    }
                }
            }*/
        }
        // !!!Important!!!
        // If this is commented, persistence does not work
        //db.commit();
        KSession.fireAllRules();
        return null;
    }

    public String generateRandomDirection() {
        int i = (new Random()).nextInt(4);
        return switch (i) {
            case 1 -> "s";
            case 2 -> "e";
            case 3 -> "w";
            default -> "n";
        };
    }
}
