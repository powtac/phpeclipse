<?xml version='1.0'?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">

<xsl:import href="file:///c:/docbook/docbook-xsl/htmlhelp/htmlhelp.xsl"/>

<xsl:param name="profile.role" select="'html;bold'"/>

<xsl:param name="paper.type" select="'A4'"/>
<xsl:param name="double.sided" select="1"/>
<xsl:param name="section.autolabel" select="1"/>
<xsl:param name="toc.section.depth" select="2"/>
<xsl:param name="section.label.includes.component.label" select="1"/>
<xsl:param name="draft.watermark.image" select="''"/>
<xsl:param name="draft.mode" select="'no'"/>
<xsl:param name="htmlhelp.use.hhk" select="1"/>
<xsl:param name="suppress.navigation" select="0"/>

<xsl:param name="tablecolumns.extension" select="'0'"></xsl:param>

<xsl:param name="callout.graphics">1</xsl:param>
<xsl:param name="callout.graphics.extension">.png</xsl:param>
<xsl:param name="callout.graphics.path" select="'img/callouts/'"></xsl:param>

<xsl:param name="admon.graphics" select="1"></xsl:param>
<xsl:param name="admon.graphics.path">img/admon/</xsl:param>

<xsl:param name="qanda.defaultlabel">number</xsl:param>
<xsl:param name="header.rule" select="0"/>
<xsl:param name="footer.rule" select="0"/>

<xsl:param name="formal.title.placement">
figure after
example before
equation after
table after
procedure before
</xsl:param>

<xsl:template name="user.head.content">
  <link rel="stylesheet" type="text/css" media="screen" href="style.css" />
  <link rel="stylesheet" type="text/css" media="print" href="xoopsstyleprint.css" />
</xsl:template>

<xsl:template name="header.navigation">
  <xsl:param name="prev" select="/foo"/>
  <xsl:param name="next" select="/foo"/>
  <xsl:param name="nav.context"/>

  <xsl:variable name="home" select="/*[1]"/>
  <xsl:variable name="up" select="parent::*"/>

  <xsl:variable name="row1" select="$navig.showtitles != 0"/>
  <xsl:variable name="row2" select="count($prev) &gt; 0
                                    or (count($up) &gt; 0
                                        and $up != $home
                                        and $navig.showtitles != 0)
                                    or count($next) &gt; 0"/>

  <xsl:if test="$suppress.navigation = '0' and $suppress.header.navigation = '0'">
    <div class="navheader">
      <xsl:if test="$row1 or $row2">
        <table width="100%" summary="Navigation header">
          <xsl:if test="$row1">
            <tr>
              <th colspan="3" align="left">
                <xsl:apply-templates select="." mode="object.title.markup"/>
              </th>
            </tr>
          </xsl:if>

          <xsl:if test="$row2">
            <tr>

              <th width="60%" align="left">
                <xsl:choose>
                  <xsl:when test="count($up) &gt; 0
                                  and $up != $home
                                  and $navig.showtitles != 0">
                    <xsl:apply-templates select="$up" mode="object.title.markup"/>
                  </xsl:when>
                  <xsl:otherwise>&#160;</xsl:otherwise>
                </xsl:choose>
              </th>

              <td width="20%" align="right">
                <font face="Arial" size="2">

                <xsl:choose>
                  <xsl:when test="count($up)&gt;0">
                    <a accesskey="u" onmouseover="document.images.main_head.src='img/nav/button_main_h.gif'" onmouseout="document.images.main_head.src='img/nav/button_main.gif'">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$up"/>
                        </xsl:call-template>
                      </xsl:attribute>
<!--
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'up'"/>
                      </xsl:call-template>
-->
                      <img name="main_head" src="img/nav/button_main.gif" border="0" alt="Back to Top" />
                    </a>

                  </xsl:when>
                  <xsl:otherwise>&#160;</xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                  <xsl:when test="count($prev)&gt;0">
                    <a accesskey="p" onmouseover="document.images.prev_head.src='img/nav/button_prev_h.gif'" onmouseout="document.images.prev_head.src='img/nav/button_prev.gif'">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$prev"/>
                        </xsl:call-template>
                      </xsl:attribute>
<!--
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'prev'"/>
                      </xsl:call-template>
-->
                      <img name="prev_head" src="img/nav/button_prev.gif" border="0" alt="Previous page" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <img src="img/nav/button_prev_d.gif" border="0" />
                  </xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                  <xsl:when test="count($next)&gt;0">
                    <a accesskey="n" onmouseover="document.images.next.src='img/nav/button_next_h.gif'" onmouseout="document.images.next.src='img/nav/button_next.gif'">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$next"/>
                        </xsl:call-template>
                      </xsl:attribute>
<!--
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'next'"/>
                      </xsl:call-template>
-->
                      <img name="next" src="img/nav/button_next.gif" border="0" alt="Next page" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <img src="img/nav/button_next_d.gif" border="0" />
                  </xsl:otherwise>
                </xsl:choose>
                </font>
              </td>
            </tr>
          </xsl:if>
        </table>
      </xsl:if>
      <xsl:if test="$header.rule != 0">
        <hr/>
      </xsl:if>
    </div>
  </xsl:if>
</xsl:template>



<xsl:template name="footer.navigation">
  <xsl:param name="prev" select="/foo"/>
  <xsl:param name="next" select="/foo"/>
  <xsl:param name="nav.context"/>

  <xsl:variable name="home" select="/*[1]"/>
  <xsl:variable name="up" select="parent::*"/>

  <xsl:variable name="row1" select="count($prev) &gt; 0
                                    or count($up) &gt; 0
                                    or count($next) &gt; 0"/>

  <xsl:variable name="row2" select="($prev and $navig.showtitles != 0)
                                    or ($home != . or $nav.context = 'toc')
                                    or ($chunk.tocs.and.lots != 0
                                        and $nav.context != 'toc')
                                    or ($next and $navig.showtitles != 0)"/>

  <xsl:if test="$suppress.navigation = '0' and $suppress.footer.navigation = '0'">
    <div class="navfooter">
      <xsl:if test="$footer.rule != 0">
        <hr/>
      </xsl:if>

      <xsl:if test="$row1 or $row2">
        <table width="100%" summary="Navigation footer">
          <xsl:if test="$row1">
            <tr>
              <td width="40%" align="left">
                <xsl:choose>
                  <xsl:when test="count($prev)&gt;0">
                    <a accesskey="p" onmouseover="document.images.prev_foot.src='img/nav/button_prev_h.gif'" onmouseout="document.images.prev_foot.src='img/nav/button_prev.gif'">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$prev"/>
                        </xsl:call-template>
                      </xsl:attribute>
<!--
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'prev'"/>
                      </xsl:call-template>
-->
                      <img name="prev_foot" src="img/nav/button_prev.gif" border="0" alt="Previous page" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <img src="img/nav/button_prev_d.gif" border="0" />
                  </xsl:otherwise>
                </xsl:choose>
              </td>
              <td width="20%" align="center">
                <xsl:choose>
                  <xsl:when test="count($up)&gt;0">
                    <a accesskey="u" onmouseover="document.images.main_foot.src='img/nav/button_main_h.gif'" onmouseout="document.images.main_foot.src='img/nav/button_main.gif'">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$up"/>
                        </xsl:call-template>
                      </xsl:attribute>
<!--
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'up'"/>
                      </xsl:call-template>
-->
                      <img name="main_foot" src="img/nav/button_main.gif" border="0" alt="Back to Top" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>&#160;</xsl:otherwise>
                </xsl:choose>
              </td>
              <td width="40%" align="right">
                <xsl:text>&#160;</xsl:text>
                <xsl:choose>
                  <xsl:when test="count($next)&gt;0">
                    <a accesskey="n" onmouseover="document.images.next_foot.src='img/nav/button_next_h.gif'" onmouseout="document.images.next_foot.src='img/nav/button_next.gif'">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$next"/>
                        </xsl:call-template>
                      </xsl:attribute>
<!--
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'next'"/>
                      </xsl:call-template>
-->
                      <img name="next_foot" src="img/nav/button_next.gif" border="0" alt="Next page" />
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <img src="img/nav/button_next_d.gif" border="0" />
                  </xsl:otherwise>
                </xsl:choose>
              </td>
            </tr>
          </xsl:if>

          <xsl:if test="$row2">
            <tr>
              <td width="40%" align="left" valign="top">
                <xsl:if test="$navig.showtitles != 0">
                  <xsl:apply-templates select="$prev" mode="object.title.markup"/>
                </xsl:if>
                <xsl:text>&#160;</xsl:text>
              </td>
              <td width="20%" align="center">
                <xsl:choose>
                  <xsl:when test="$home != . or $nav.context = 'toc'">
                    <a accesskey="h">
                      <xsl:attribute name="href">
                        <xsl:call-template name="href.target">
                          <xsl:with-param name="object" select="$home"/>
                        </xsl:call-template>
                      </xsl:attribute>
                      <xsl:call-template name="navig.content">
                        <xsl:with-param name="direction" select="'home'"/>
                      </xsl:call-template>
                    </a>
                    <xsl:if test="$chunk.tocs.and.lots != 0 and $nav.context != 'toc'">
                      <xsl:text>&#160;|&#160;</xsl:text>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>&#160;</xsl:otherwise>
                </xsl:choose>

                <xsl:if test="$chunk.tocs.and.lots != 0 and $nav.context != 'toc'">
                  <a accesskey="t">
                    <xsl:attribute name="href">
                      <xsl:apply-templates select="/*[1]" mode="recursive-chunk-filename"/>
                      <xsl:text>-toc</xsl:text>
                      <xsl:value-of select="$html.ext"/>
                    </xsl:attribute>
                    <xsl:call-template name="gentext">
                      <xsl:with-param name="key" select="'nav-toc'"/>
                    </xsl:call-template>
                  </a>
                </xsl:if>
              </td>
              <td width="40%" align="right" valign="top">
                <xsl:text>&#160;</xsl:text>
                <xsl:if test="$navig.showtitles != 0">
                  <xsl:apply-templates select="$next" mode="object.title.markup"/>
                </xsl:if>
              </td>
            </tr>
          </xsl:if>
        </table>
      </xsl:if>
    </div>
  </xsl:if>
</xsl:template>


</xsl:stylesheet>
