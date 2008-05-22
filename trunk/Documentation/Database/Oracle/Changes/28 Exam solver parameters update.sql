/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


/**
 * Add Exams.RoomSplitDistanceWeight and Exam.Large weights
 **/

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.RoomSplitDistanceWeight' as name,
		  '0.01' as default_vale,
		  'If an examination in split between two or more rooms, weight for an average distance between these rooms' as description,
		  'double' as type,
		  18 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.LargeSize' as name,
		  '-1' as default_vale,
		  'Large Exam Penalty: minimal size of a large exam (disabled if -1)' as description,
		  'integer' as type,
		  19 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
		   
insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.LargePeriod' as name,
		  '0.67' as default_vale,
		  'Large Exam Penalty: first discouraged period = number of periods x this factor' as description,
		  'double' as type,
		  20 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.LargeWeight' as name,
		  '1.0' as default_vale,
		  'Large Exam Penalty: weight of a large exam that is assigned on or after the first discouraged period' as description,
		  'double' as type,
		  21 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
 
/*
 * Update database version
 */

update application_config set value='28' where name='tmtbl.db.version';

commit;
