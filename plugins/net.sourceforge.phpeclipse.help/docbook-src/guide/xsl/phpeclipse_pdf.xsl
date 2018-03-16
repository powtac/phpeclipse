<?xml version='1.0' encoding='ISO-8859-1'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:exsl="http://exslt.org/common"
                extension-element-prefixes="exsl"
                exclude-result-prefixes="exsl"
                version="1.0">

<xsl:import href="file:///c:/docbook/docbook-xsl/fo/profile-docbook.xsl"/>
<xsl:import href="file:///c:/docbook/docbook-xsl/fo/docbook.xsl"/>
<xsl:import href="phpeclipse_pagemaster.xsl"/>

<xsl:param name="profile.role" select="'with_index;fo;bold'"/>

<xsl:param name="paper.type" select="'A4'"/>
<xsl:param name="double.sided" select="1"/>
<xsl:param name="section.autolabel" select="1"/>
<xsl:param name="toc.section.depth" select="2"/>
<xsl:param name="section.label.includes.component.label" select="1"/>
<xsl:param name="draft.watermark.image" select="''"/>
<xsl:param name="draft.mode" select="'no'"/>
<xsl:param name="fop.extensions" select="1"/>

<xsl:param name="headers.on.blank.pages" select="1"/>
<xsl:param name="header.rule" select="1"/>
<xsl:param name="line-height" select="'normal'"/>

<xsl:param name="saxon.extensions" select="1"/>

<xsl:param name="use.extensions"  select="1"/>
<xsl:param name="callouts.extension"  select="1"/>
<xsl:param name="textinsert.extension" select="1"/>
<xsl:param name="tablecolumns.extension" select="'0'"></xsl:param>

<xsl:param name="callout.graphics">1</xsl:param>
<xsl:param name="callout.graphics.extension">.png</xsl:param>
<xsl:param name="callout.graphics.path" select="'img/callouts/'"></xsl:param>

<xsl:param name="admon.graphics" select="1"></xsl:param>
<xsl:param name="admon.graphics.path">img/admon/</xsl:param>

<xsl:param name="qanda.defaultlabel">none</xsl:param>

<xsl:param name="insert.xref.page.number" select="1"/>
<!--
<xsl:param name="footnote.number.format" select="i"></xsl:param>
<xsl:param name="footnote.number.symbols" select="*"></xsl:param>
-->

<xsl:param name="body.margin.top" select="'13mm'"/>
<xsl:param name="body.margin.bottom" select="'5mm'"/>

<xsl:param name="region.before.extent" select="'7.80mm'"/>
<xsl:param name="region.after.extent" select="'5mm'"/>

<xsl:param name="region.side.extent" select="'30mm'"/>
<xsl:param name="region.side.margin" select="'4mm'"/>

<xsl:param name="sidebar.background.color" select="'#eaECF0'"/>

<!-- use vertical-align instead of baseline-shift (baseline-shift doesn't work with fop!) -->

<xsl:template name="format.footnote.mark">
  <xsl:param name="mark" select="'?'"/>
  <fo:inline vertical-align="super" font-size="75%">
    <xsl:copy-of select="$mark"/>
  </fo:inline>
</xsl:template>


<!-- title.margin.left gives the distance between start of chapter and sections title and normal text flow. -->
<!-- with -0pc the titles and the text flow begin at the same column. -->
<xsl:param name="title.margin.left" select="'-0pc'"/>

<!-- ==================================================================== -->

<xsl:param name="page.margin.inner">
  <xsl:choose>
    <xsl:when test="$double.sided != 0">5mm</xsl:when>
    <xsl:otherwise>5mm</xsl:otherwise>
  </xsl:choose>
</xsl:param>

<!-- ==================================================================== -->

<xsl:param name="page.margin.outer">
  <xsl:choose>
    <xsl:when test="$double.sided != 0">15mm</xsl:when>
    <xsl:otherwise>20mm</xsl:otherwise>
  </xsl:choose>
</xsl:param>

<!-- ==================================================================== -->

<xsl:param name="formal.title.placement">
figure after
example before
equation after
table after
procedure before
</xsl:param>

<!-- ==================================================================== -->

<xsl:template name="book.titlepage">
  <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <fo:block>
    <xsl:call-template name="book.titlepage.before.recto"/>
    <xsl:call-template name="book.titlepage.recto"/>
    </fo:block>
    <fo:block>
    <xsl:call-template name="book.titlepage.before.verso"/>
    <xsl:call-template name="book.titlepage.verso"/>
    </fo:block>
<!--
    <xsl:call-template name="book.titlepage.separator"/>
-->
  </fo:block>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="header.content">
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="position" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

  <xsl:variable name="candidate">
    <!-- sequence can be odd, even, first, blank -->
    <!-- position can be left, center, right -->
    <xsl:choose>
<!--
      <xsl:when test="$sequence = 'odd' and $position = 'left'">
        <fo:retrieve-marker retrieve-class-name="section.head.marker"
                            retrieve-position="first-including-carryover"
                            retrieve-boundary="page"/>
      </xsl:when>
-->
      <xsl:when test="($sequence = 'odd' or $sequence = 'even' or
                       $sequence = 'blank' or $sequence = 'first') and $position = 'center'">
      </xsl:when>

      <xsl:when test="($sequence = 'odd'   and $position = 'right') or
                      ($sequence = 'even'  and $position = 'left') or
                      ($sequence = 'blank' and $position = 'left') or
                      ($sequence = 'first' and $position = 'right')">
        <xsl:choose>
          <xsl:when test="name(/book[position() = 1]) = 'book'">
            <xsl:value-of select="ancestor-or-self::book/bookinfo/edition"/>
          </xsl:when>

          <xsl:when test="name(/article[position() = 1]) = 'article'">
            <xsl:value-of  select="ancestor-or-self::article/articleinfo/edition"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="name(/book[position() = 1])"/>
            <xsl:value-of select="name(/article[position() = 1])"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:when test="($sequence = 'odd'   and $position = 'left') or
                      ($sequence = 'even'  and $position = 'right') or
                      ($sequence = 'blank' and $position = 'right') or
                      ($sequence = 'first' and $position = 'left')">
        <xsl:choose>
          <xsl:when test="name(/book[position() = 1]) = 'book'">
            <xsl:value-of select="ancestor-or-self::book/bookinfo/title"/>
          </xsl:when>

          <xsl:when test="name(/article[position() = 1]) = 'article'">
            <xsl:value-of select="ancestor-or-self::article/articleinfo/title"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="name(/book[position() = 1])"/>
            <xsl:value-of select="name(/article[position() = 1])"/>
          </xsl:otherwise>
       </xsl:choose>

      </xsl:when>
<!--
      <xsl:when test="$sequence = 'even' and $position = 'right'">
         <fo:block><xsl:apply-templates select="." mode="object.title.markup"/></fo:block>
      </xsl:when>
-->

      <xsl:when test="$sequence = 'first' and $position = 'left'">
      </xsl:when>

      <xsl:when test="$sequence = 'first' and $position = 'right'">
      </xsl:when>

      <xsl:when test="$sequence = 'first' and $position = 'center'">
        <xsl:value-of select="ancestor-or-self::book/bookinfo/corpauthor"/>
      </xsl:when>

      <xsl:when test="$sequence = 'blank' and $headers.on.blank.pages = 1">
        <xsl:choose>
          <xsl:when test="$position = 'left'">
          </xsl:when>

          <xsl:when test="$position = 'center'">
            <!--  <xsl:text>This page intentionally left blank</xsl:text> -->
          </xsl:when>

          <xsl:when test="$position = 'right'">
          </xsl:when>
        </xsl:choose>
      </xsl:when>

    </xsl:choose>
  </xsl:variable>

  <!-- Does runtime parameter turn off blank page headers? -->
  <xsl:choose>
    <xsl:when test="$sequence='blank' and $headers.on.blank.pages=0">
      <!-- no output -->
    </xsl:when>

    <xsl:when test="$pageclass = 'titlepage'">
      <!-- titlepages have no headers -->
    </xsl:when>

    <xsl:otherwise>
      <xsl:copy-of select="$candidate"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="footer.content">
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="position" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

  <fo:block>
    <!-- pageclass can be front, body, back      -->
    <!-- sequence can be odd, even, first, blank -->
    <!-- position can be left, center, right     -->

    <xsl:choose>
      <xsl:when test="$pageclass = 'titlepage'">
        <!-- nop; no footer on title pages -->
      </xsl:when>

      <!--
      <xsl:when test="($sequence='even' and $double.sided != 0 and $position='left') or
                      ($sequence='odd' and $double.sided != 0 and $position='right')">
        page <fo:page-number/> of <fo:page-number-citation ref-id="TheVeryLastPage"/>
      </xsl:when>
      -->
      <xsl:when test="($sequence='even'  and $position='left') or
                      ($sequence='odd'   and $position='right') or
                      ($sequence='first' and $position='right') or
                      ($sequence='blank' and $position='left')">
        <fo:page-number/>
      </xsl:when>

      <xsl:when test="($sequence='even'  and $position='right') or
                      ($sequence='odd'   and $position='left') or
                      ($sequence='first' and $position='left') or
                      ($sequence='blank' and $position='right')">
        <xsl:choose>
          <xsl:when test="name(/book[position() = 1]) = 'book'">
            <xsl:value-of select="ancestor-or-self::book/bookinfo/orgname"/>
          </xsl:when>

          <xsl:when test="name(/article[position() = 1]) = 'article'">
            <xsl:value-of select="ancestor-or-self::article/articleinfo/orgname"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="name(/book[position() = 1])"/>
            <xsl:value-of select="name(/article[position() = 1])"/>
          </xsl:otherwise>
       </xsl:choose>

      </xsl:when>

      <xsl:when test="$position='center'">
        <xsl:choose>
          <xsl:when test="name(/book[position() = 1]) = 'book'">
            <xsl:value-of select="ancestor-or-self::book/bookinfo/pubdate"/>
          </xsl:when>

          <xsl:when test="name(/article[position() = 1]) = 'article'">
            <xsl:value-of select="ancestor-or-self::article/articleinfo/pubdate"/>
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="name(/book[position() = 1])"/>
            <xsl:value-of select="name(/article[position() = 1])"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:otherwise>
        <!-- nop -->
      </xsl:otherwise>
    </xsl:choose>
  </fo:block>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="inline.frameseq">
  <xsl:param name="content">
    <xsl:apply-templates/>
  </xsl:param>
  <fo:inline font-weight="bold" color="#555555">
    <xsl:if test="@dir">
      <xsl:attribute name="direction">
        <xsl:choose>
          <xsl:when test="@dir = 'ltr' or @dir = 'lro'">ltr</xsl:when>
          <xsl:otherwise>rtl</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:if>
    [<xsl:copy-of select="$content"/>]
  </fo:inline>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="inline.highlightsseq">
  <xsl:param name="content">
    <xsl:apply-templates/>
  </xsl:param>
  <fo:block>
  <fo:inline font-weight="bold" font-size="125%">
    <xsl:if test="@dir">
      <xsl:attribute name="direction">
        <xsl:choose>
          <xsl:when test="@dir = 'ltr' or @dir = 'lro'">ltr</xsl:when>
          <xsl:otherwise>rtl</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:if>
    <xsl:copy-of select="$content"/>
  </fo:inline>
  </fo:block>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template  match="guibutton">
  <xsl:call-template  name="inline.frameseq"/>
</xsl:template>


<xsl:template  match="guilabel">
  <xsl:call-template  name="inline.frameseq"/>
</xsl:template>


<xsl:template  match="database">
  <xsl:call-template  name="inline.boldseq"/>
</xsl:template>


<xsl:template  match="highlights">
  <xsl:call-template  name="inline.highlightsseq"/>
</xsl:template>

<!-- ==================================================================== -->

<xsl:template name="inline.monoseq">
   <xsl:param name="content">
     <xsl:apply-templates/>
   </xsl:param>
   <fo:inline xsl:use-attribute-sets="monospace.properties">
     <xsl:if test="@dir">
       <xsl:attribute name="direction">
         <xsl:choose>
           <xsl:when test="@dir = 'ltr' or @dir = 'lro'">ltr</xsl:when>
           <xsl:otherwise>rtl</xsl:otherwise>
         </xsl:choose>
       </xsl:attribute>
     </xsl:if>
     <xsl:apply-templates select="exsl:node-set($content)" mode="hyphenate"/>
   </fo:inline>
</xsl:template>

<xsl:template match="text()" mode="hyphenate" priority="2">
   <xsl:call-template name="string.subst">
     <xsl:with-param name="string">
       <xsl:call-template name="string.subst">
         <xsl:with-param name="string">
           <xsl:call-template name="string.subst">
             <xsl:with-param name="string" select="."/>
             <xsl:with-param name="target" select="'.'"/>
             <xsl:with-param name="replacement" select="'.&#x200B;'"/>
           </xsl:call-template>
         </xsl:with-param>
         <xsl:with-param name="target" select="'\'"/>
         <xsl:with-param name="replacement" select="'\&#x200B;'"/>
       </xsl:call-template>
     </xsl:with-param>
     <xsl:with-param name="target" select="'/'"/>
     <xsl:with-param name="replacement" select="'/&#x200B;'"/>
   </xsl:call-template>
</xsl:template>

<xsl:template match="node()|@*" mode="hyphenate">
   <xsl:copy>
     <xsl:apply-templates select="node()|@*" mode="hyphenate"/>
   </xsl:copy>
</xsl:template>

<!-- ==================================================================== -->

</xsl:stylesheet>
