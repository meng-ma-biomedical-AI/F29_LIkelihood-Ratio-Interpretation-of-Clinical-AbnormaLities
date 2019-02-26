package org.monarchinitiative.lr2pg.cmd;

import com.beust.jcommander.Parameter;
import org.monarchinitiative.lr2pg.analysis.Gene2Genotype;
import org.monarchinitiative.lr2pg.configuration.Lr2PgFactory;
import org.monarchinitiative.lr2pg.hpo.HpoCase;
import org.monarchinitiative.lr2pg.output.HtmlTemplate;
import org.monarchinitiative.lr2pg.output.Lr2pgTemplate;
import org.monarchinitiative.lr2pg.output.TsvTemplate;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Map;

/**
 * This is a common superclass for {@link VcfCommand} and {@link PhenopacketCommand}.
 * Its purpose is to provide command line parameters and variables that are used
 * in the same way by both of the subclasses.
 */
public abstract class PrioritizeCommand extends Lr2PgCommand {
    /** Directory where various files are downloaded/created. */
    @Parameter(names={"-d","--data"}, description ="directory to download data" )
    protected String datadir="data";
    /** Discard any candidate disease with no known disease gene or for which no predicted pathogenic variant
     * was found in the corresponding disease gene. */
    @Parameter(names={"-k","--keep"}, description = "retain candidates even if no candidate variant is found")
    protected boolean keepIfNoCandidateVariant=false;
    @Parameter(names={"-m","--mindiff"}, description = "minimal number of differential diagnoses to show")
    protected int minDifferentialsToShow=5;
    /** If true, filter VCF lines by the FILTER column (variants pass if there is no entry, i.e., ".",
     * or if the value of the field is FALSE. Variant also fail if a reason for the not passing the
     * filter is given in the column, i.e., for allelic imbalance. This is true by default. Filtering
     * can be turned off by entering {@code -q false} or {@code --quality} false. */
    @Parameter(names={"-f","--filter"},description = "filter on VCF FILTER quality",arity = 1)
    protected boolean filterOnFILTER=true;
    /** The threshold for showing a differential diagnosis in the main section (posterior probability of 1%).*/
    @Parameter(names= {"-t","--threshold"}, description = "minimum post-test prob. to show diagnosis in HTML output")
    protected double LR_THRESHOLD=0.01;
    @Parameter(names={"--transcriptdb"}, description = "transcript database (UCSC, Ensembl, RefSeq)")
    protected String transcriptDb="ucsc";
    /** If true, the program will not output an HTML file but will output a Tab Separated Values file instead.*/
    @Parameter(names="--tsv",description = "Use TSV instead of HTML output")
    protected boolean outputTSV=false;
    /** Prefix of the output file. For instance, if the user enters {@code -x sample1} and an HTML file is output,
     * the name of the HTML file will be {@code sample1.html}. If a TSV file is output, the name of the file will
     * be {@code sample1.tsv}. */
    @Parameter(names={"-x", "--prefix"},description = "prefix of outfile")
    protected String outfilePrefix="lr2pg";
    /** An object that contains parameters from the YAML file for configuration. */
    protected Lr2PgFactory factory;
    /** Key: an EntrezGene id; value: corresponding gene symbol. */
    protected Map<TermId,String> geneId2symbol;
    /** Various metadata that will be used for the HTML org.monarchinitiative.lr2pg.output. */
    protected Map<String,String> metadata;

    /**
     * Output a summary of results as an HTML file. This function should be used for cases
     * in which exome/genome data is available, i.e., phenotype-driven genome/exome analysis
     * @param hcase Reference to the HPO Case
     * @param ontology Reference to HPO Ontology object
     * @param genotypeMap Map with results of genotype analysis for each gene
     */
    protected void outputHTML(HpoCase hcase, Ontology ontology, Map<TermId, Gene2Genotype> genotypeMap) {
        HtmlTemplate caseoutput = new HtmlTemplate(hcase,
                ontology,
                genotypeMap,
                this.geneId2symbol,
                this.metadata,
                this.LR_THRESHOLD,
                minDifferentialsToShow);
        caseoutput.outputFile(this.outfilePrefix);
    }

    /**
     * Output a summary of results as an HTML file. This function should be used for cases
     * in which no exome/genome data is available, i.e., phenotype-only analysis.
     * @param hcase Reference to the HPO Case
     * @param ontology Reference to HPO Ontology object
     */
    protected void outputHTML(HpoCase hcase, Ontology ontology) {
        HtmlTemplate caseoutput = new HtmlTemplate(hcase, ontology,
                this.metadata,
                this.LR_THRESHOLD,
                this.minDifferentialsToShow);
        caseoutput.outputFile(this.outfilePrefix);
    }


    /**
     * Output a tab-separated values file with one line per differential diagnosis. This
     * function should be used when exome/genome data is available
     * @param hcase Reference to the HPO Case
     * @param ontology Reference to HPO Ontology object
     * @param genotypeMap Map with results of genotype analysis for each gene
     */
    protected void outputTSV(HpoCase hcase,Ontology ontology,Map<TermId, Gene2Genotype> genotypeMap) {
        Lr2pgTemplate template = new TsvTemplate(hcase,ontology,genotypeMap,this.geneId2symbol,this.metadata);
        template.outputFile(this.outfilePrefix);
    }

    /**
     * Output a tab-separated values file with one line per differential diagnosis. This function should be used for cases
     *      * in which no exome/genome data is available, i.e., phenotype-only analysis.
     * @param hcase Reference to the HPO Case
     * @param ontology Reference to HPO Ontology object

     */
    protected void outputTSV(HpoCase hcase,Ontology ontology) {
        Lr2pgTemplate template = new TsvTemplate(hcase, ontology, this.metadata);
        template.outputFile(this.outfilePrefix);
    }


}