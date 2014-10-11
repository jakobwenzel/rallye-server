<?xml version="1.0"?>
 
<!--
  ~ Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
  ~
  ~ This file is part of RallySoft.
  ~
  ~ RallyeSoft is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ RallyeSoft is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
  -->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns="http://www.w3.org/2000/svg"
		>

	<!-- template for one event -->
	<xsl:template match="event">
        <!--<sum>
            <xsl:value-of select="sum(.|preceding-sibling::number)"/>
        </sum>-->
		<text x="0mm" y="{position()*3}mm" font-size="15" fill="#FFF"><xsl:value-of select="title" />           
		<xsl:value-of select="sum((.|preceding-sibling::event)/duration)"/></text>

	</xsl:template>
			
	<xsl:template match="/">
		<svg xmlns="http://www.w3.org/2000/svg" version="1.2" width="176mm" height="131mm" >

			<style type="text/css" >
				<![CDATA[

					svg{
						font-family: "Museo 500", Museo;
					}
					.heading {
						font-family: "Museo 700", "Museo";
						font-size: 28px;
					}
					.day {
						font-size: 12px;
					}
					
					.entry .time {
						font-size: 8px;
						fill: #FFFFFF;
					}
					.entry .location {
						font-size: 7px;
					}
					.entry .main {
						font-size: 11px;
						text-anchor: middle;
					}
					
					
					
					.entry-evening .shadow {
						fill: #508D00;
						fill-opacity: 0.196;
					}
					.entry-evening .mainbg {
						fill: #84E900;
						fill-opacity: 0.3922;
					}
					.entry-evening .timebg {
						fill: #66B400;
					}
					.entry-evening .locationbg {
						fill: #9BEA34;
					}

					.entry-spacer {
						fill-opacity: 0;
					}
					
					.entry-plenum .shadow {
						fill: #9B7300;
						fill-opacity: 0.196;
					}
					.entry-plenum .mainbg {
						fill: #FFCC39;
						fill-opacity: 0.3922;
					}
					.entry-plenum .timebg {
						fill: #C59300;
					}
					.entry-plenum .locationbg {
						fill: #FFBE00;
					}
					

					
					.entry-kleingruppe .shadow {
						fill: #440467;
						fill-opacity: 0.196;
					}
					.entry-kleingruppe .mainbg {
						fill: #7109AA;
						fill-opacity: 0.3922;
					}
					.entry-kleingruppe .timebg {
						fill: #570783;
					}
					.entry-kleingruppe .locationbg {
						fill: #812EAF;
					}
					
					.entry-mensa .shadow {
						fill: #9B2300;
						fill-opacity: 0.196;
					}
					.entry-mensa .mainbg {
						fill: #FF3900;
						fill-opacity: 0.3922;
					}
					.entry-mensa .timebg {
						fill: #C52C00;
					}
					.entry-mensa .locationbg {
						fill: #FF6639;
					}
					
					.entry-other .shadow {
						fill: #006D33;
						fill-opacity: 0.196;
					}
					.entry-other .mainbg {
						fill: #00B454;
						fill-opacity: 0.3922;
					}
					.entry-other .timebg {
						fill: #008B41;
					}
					.entry-other .locationbg {
						fill: #29B96C;
					}
					
					
					.entry-aktiv .shadow {
						fill: #044063;
						fill-opacity: 0.196;
					}
					.entry-aktiv .mainbg {
						fill: #096AA3;
						fill-opacity: 0.3922;
					}
					.entry-aktiv .timebg {
						fill: #07517D;
					}
					.entry-aktiv .locationbg {
						fill: #2D7AA8;
				]]>
			</style>
		
			<!-- debugging background -->
			<!--rect x="-500" y="-500" width="2000" height="2000" fill="#f00" /-->
			
			<!-- heading -->
			<text x="88mm" y="10mm" font-size="30" text-anchor="middle" class="heading">Stundenplan <xsl:value-of select="timetable/heading" /></text>
			
			<!-- timeslots -->
			<svg y="19mm">
				<xsl:for-each select="timetable/timeslots/timeslot">
						<svg y="{position()*10-10}mm">
							<rect x="1mm" y="1mm" width="15mm" height="8mm" fill="#DCDCDC" />
							<rect x="0mm" y="0mm" width="15mm" height="8mm" fill="#535353" />
							
							<text x="0.75mm" y="3.5mm" fill="#DCDCDC" style="font-size: 12px" ><xsl:value-of select="from" /> - </text>
							<text x="14.25mm" y="7mm" fill="#DCDCDC" text-anchor="end" style="font-size: 10px"><xsl:value-of select="to" /></text>
						</svg>
					
				</xsl:for-each>
			</svg>
			
			<!-- each day -->
			<xsl:for-each select="timetable/days/day">

				<!-- group with offset -->
				<svg x="{position()*32-(32-17)}mm" y="12mm"  >
					<!--rect x="0" y="0" width="32mm" height="2000" fill="#0{position()}F" /-->

					
					<rect x="1mm" y="1mm" width="30mm" height="5mm" fill="#DCDCDC" />
					<rect x="0mm" y="0mm" width="30mm" height="5mm" fill="#535353" />
					
				
					<text x="15mm" y="3.7mm" font-size="15" fill="#DCDCDC" class="day" text-anchor="middle" ><xsl:value-of select="heading" /></text>
					
					
					<!-- events -->
					<xsl:for-each select="events/event">
						<xsl:variable name="totalDur"><xsl:value-of select="sum((.|preceding-sibling::event)/duration)-duration"/></xsl:variable>
						<svg y="{$totalDur*10+7}mm" class="entry entry-{type}" >
							
							<!-- Main Background -->
							<rect x="0mm" y="3mm" width="30mm" height="{duration*10-5}mm" class="mainbg"/>
							<!-- Shadow -->
							<rect x="30mm" y="1mm" width="1mm" height="{duration*10-2}mm" class="shadow"/>
							<rect x="1mm" y="{duration*10-2}mm" width="29mm" height="1mm" class="shadow"/>
							<!-- Time Background --> 
							<rect x="0mm" y="0mm" width="8mm" height="3mm" class="timebg" /> 
							<!-- Location Background -->
							<rect x="8mm" y="0mm" width="22mm" height="3mm" class="locationbg" />
							
							<!-- Time -->
							<text x="4mm" y="2.3mm" class="time"  text-anchor="middle"><xsl:value-of select="starttime"/></text>
							<!-- Location -->
							<text x="29mm" y="2.3mm" class="location"  text-anchor="end"><xsl:value-of select="location"/></text>
							<!-- Title -->
							<flowRoot>
								<flowRegion>
									<rect x="0" y="4mm" width="30mm" height="{duration*10-5.5}mm"
										fill="#FFCC39" style="fill-opacity: 0.0"/>
									</flowRegion>
								<flowDiv>
									<flowPara font-size="11px" text-align="middle" >
										<xsl:value-of select="title" /> 
									</flowPara>
								</flowDiv>
							</flowRoot>
							<!--text x="0mm" y="8mm" font-size="15" fill="#FFF"><xsl:value-of select="title" />   <xsl:value-of select="$totalDur"/></text-->
							
					
						</svg>
					
					</xsl:for-each>
					<!--<xsl:apply-templates select="events" />-->
					
				</svg>
			</xsl:for-each>
		</svg>
	</xsl:template>
</xsl:stylesheet>
