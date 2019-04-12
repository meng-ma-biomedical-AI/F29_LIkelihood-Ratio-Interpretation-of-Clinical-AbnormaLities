package org.monarchinitiative.lr2pg.output;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.monarchinitiative.lr2pg.analysis.Gene2Genotype;
import org.monarchinitiative.lr2pg.hpo.HpoCase;
import org.monarchinitiative.lr2pg.likelihoodratio.TestResult;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class coordinates the output of a TSV file that contains a suymmary of the analysis results.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class TsvTemplate extends Lr2pgTemplate {
    private static final Logger logger = LoggerFactory.getLogger(TsvTemplate.class);

    private static final String[] tsvHeader={"rank","diseaseName","diseaseCurie","pretestprob","posttestprob",
            "compositeLR","entrezGeneId","variants"};



    public TsvTemplate(HpoCase hcase,
                       Ontology ontology,
                       Map<TermId, Gene2Genotype> genotypeMap,
                       Map<TermId, String> geneid2sym,
                       Map<String, String> metadat) {
        super(hcase, ontology, genotypeMap, geneid2sym, metadat);
        ClassLoader classLoader = TsvTemplate.class.getClassLoader();
        cfg.setClassLoaderForTemplateLoading(classLoader,"");
        List<TsvDifferential> diff = new ArrayList<>();
        String header= String.join("\t",tsvHeader);
        templateData.put("header",header);
        int counter=0;
        // Note the following results are already sorted
        for (TestResult result : hcase.getResults()) {
            String symbol = EMPTY_STRING;
            TsvDifferential tsvdiff = new TsvDifferential(result);
            logger.trace("Diff diag for " + result.getDiseaseName());
            if (result.hasGenotype()) {
                TermId geneId = result.getEntrezGeneId();
                Gene2Genotype g2g = genotypeMap.get(geneId);
                if (g2g != null) {
                   // symbol = g2g.getSymbol();
                    tsvdiff.addG2G(g2g);
                } else {
                    tsvdiff.setNoVariantsFoundString("no variants found in " + this.geneId2symbol.get(geneId));
                    //symbol = "no variants found in " + this.geneId2symbol.get(geneId);//
                }
            } else {
                tsvdiff.setNoVariantsFoundString("No known disease gene");
            }
            diff.add(tsvdiff);
            counter++;
        }
        this.templateData.put("diff",diff);
    }

    /**
     * Constructor for when we do the analysis without genetic data
     * @param hcase The current HPO case whose results we are about to output
     * @param ontology Reference toHPO Ontology object
     * @param metadat Reference to a map of "metadata"-information we will use for the output file
     */
    public TsvTemplate(HpoCase hcase,
                       Ontology ontology,
                       Map<String, String> metadat) {
        super(hcase,ontology,metadat);
        ClassLoader classLoader = TsvTemplate.class.getClassLoader();
        cfg.setClassLoaderForTemplateLoading(classLoader,"");
        List<TsvDifferential> diff = new ArrayList<>();
        String header= String.join("\t",tsvHeader);
        templateData.put("header",header);
        int counter=0;
        // Note the following results are already sorted
        for (TestResult result : hcase.getResults()) {
            String symbol = EMPTY_STRING;
            TsvDifferential tsvdiff = new TsvDifferential(result);
            logger.trace("Diff diag for " + result.getDiseaseName());
            tsvdiff.setNoVariantsFoundString("Analysis performed without genetic data");
            diff.add(tsvdiff);
            counter++;
        }
        this.templateData.put("diff",diff);
    }


    @Override
    public void outputFile(String prefix, String outdir){
        String outname=String.format("%s.html",prefix );
        if (outdir != null) {
            File dir = mkdirIfNotExist(outdir);
            outname = Paths.get(dir.getAbsolutePath(),outname).toString();
        }
        logger.trace("Writing TSV file to {}",outname);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outname))) {
            Template template = cfg.getTemplate("lr2pgTSV.ftl");
            template.process(templateData, out);
        } catch (TemplateException | IOException te) {
            te.printStackTrace();
        }
    }
}
